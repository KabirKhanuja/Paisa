package kabir.paisa.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * In-memory mirror of the signed-in user's Firestore state. Writes are
 * `suspend` and await the Firestore round-trip before returning so callers
 * can sequence UI state behind them. Reads come from snapshot listeners
 * attached by [AuthRepository] on sign-in.
 */
object PaisaRepository {

    const val TYPE_CREDIT = "credit"
    const val TYPE_DEBIT = "debit"
    const val SOURCE_AUTO = "auto"
    const val SOURCE_MANUAL = "manual"

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _budget = MutableStateFlow(Budget())
    val budget: StateFlow<Budget> = _budget.asStateFlow()

    private val _startingBalance = MutableStateFlow(0.0)
    val startingBalance: StateFlow<Double> = _startingBalance.asStateFlow()

    val balance: Double
        get() = _startingBalance.value + _transactions.value.sumOf { it.signedAmount }

    private var currentUid: String? = null
    private var txListener: ListenerRegistration? = null
    private var budgetListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    private val dayKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun attachToUser(uid: String) {
        if (uid == currentUid) return
        detach()
        currentUid = uid

        val userDoc = userDocRef(uid)

        userListener = userDoc.addSnapshotListener { snap, _ ->
            snap?.getDouble("startingBalance")?.let { _startingBalance.value = it }
        }

        txListener = userDoc.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    _transactions.value = snap.documents.mapNotNull { doc ->
                        doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                    }
                }
            }

        budgetListener = userDoc.collection("budget").document(currentMonthKey())
            .addSnapshotListener { snap, _ ->
                _budget.value = snap?.takeIf { it.exists() }
                    ?.toObject(Budget::class.java)
                    ?: Budget()
            }
    }

    fun detach() {
        txListener?.remove(); txListener = null
        budgetListener?.remove(); budgetListener = null
        userListener?.remove(); userListener = null
        currentUid = null
        _transactions.value = emptyList()
        _budget.value = Budget()
        _startingBalance.value = 0.0
    }

    /**
     * Persist a new transaction to Firestore, await the write, then update
     * that day's [DailySnapshot] doc. Local [transactions] is updated only
     * after Firestore acknowledges.
     */
    suspend fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        note: String,
        source: String,
        date: Timestamp = Timestamp.now(),
        id: String = UUID.randomUUID().toString(),
    ): Transaction {
        val userDoc = firestoreUserDoc()
            ?: error("Cannot add transaction: no signed-in user.")

        val newBalance = balance + signFor(type) * amount
        val tx = Transaction(
            id = id,
            amount = amount,
            type = type,
            category = category,
            note = note,
            source = source,
            date = date,
            balanceAfter = newBalance,
        )

        userDoc.collection("transactions").document(tx.id).set(tx).await()
        _transactions.value = (listOf(tx) + _transactions.value).sortedByDescending { it.date.seconds }
        updateDailySnapshot(userDoc, tx)
        return tx
    }

    suspend fun tagTransaction(id: String, category: String, note: String? = null) {
        val userDoc = firestoreUserDoc() ?: return
        val updates = mutableMapOf<String, Any>("category" to category)
        if (note != null) updates["note"] = note
        userDoc.collection("transactions").document(id).update(updates).await()
        _transactions.value = _transactions.value.map {
            if (it.id == id) it.copy(category = category, note = note ?: it.note) else it
        }
    }

    suspend fun updateBudget(b: Budget) {
        val userDoc = firestoreUserDoc()
        if (userDoc != null) {
            userDoc.collection("budget").document(currentMonthKey()).set(b).await()
        }
        _budget.value = b
    }

    suspend fun setStartingBalance(amount: Double) {
        val userDoc = firestoreUserDoc()
        if (userDoc != null) {
            userDoc.update("startingBalance", amount).await()
        }
        _startingBalance.value = amount
    }

    /**
     * One-shot reload of transactions and budget from Firestore. Snapshot
     * listeners already keep state live, but call this on analytics-screen
     * entry to guarantee fresh data on tab switch.
     */
    suspend fun refreshAnalytics() {
        val userDoc = firestoreUserDoc() ?: return
        val txSnap = userDoc.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .get().await()
        _transactions.value = txSnap.documents.mapNotNull { doc ->
            doc.toObject(Transaction::class.java)?.copy(id = doc.id)
        }
        val budgetSnap = userDoc.collection("budget").document(currentMonthKey()).get().await()
        _budget.value = budgetSnap.takeIf { it.exists() }
            ?.toObject(Budget::class.java) ?: Budget()
        userDoc.get().await().getDouble("startingBalance")?.let { _startingBalance.value = it }
    }

    fun untaggedDebits(): List<Transaction> =
        _transactions.value.filter { it.category.isBlank() && it.type == TYPE_DEBIT }

    fun balanceAt(date: Date): Double {
        val cutoff = endOfDay(date).time / 1000
        return _startingBalance.value + _transactions.value
            .filter { it.date.seconds <= cutoff }
            .sumOf { it.signedAmount }
    }

    // ----- daily snapshots -----

    private suspend fun updateDailySnapshot(userDoc: DocumentReference, newTx: Transaction) {
        val dayKey = dayKeyFmt.format(newTx.date.toDate())
        val (dayStart, dayEnd) = dayBounds(newTx.date.toDate())

        val querySnap = userDoc.collection("transactions")
            .whereGreaterThanOrEqualTo("date", Timestamp(dayStart))
            .whereLessThanOrEqualTo("date", Timestamp(dayEnd))
            .get().await()
        val fromServer = querySnap.documents.mapNotNull { doc ->
            doc.toObject(Transaction::class.java)?.copy(id = doc.id)
        }
        val all = (fromServer + newTx).distinctBy { it.id }

        val totalCredit = all.filter { it.type == TYPE_CREDIT }.sumOf { it.amount }
        val totalDebit = all.filter { it.type == TYPE_DEBIT }.sumOf { it.amount }
        val closingBalance = all.maxByOrNull { it.date.seconds }?.balanceAfter ?: balance
        val signedDelta = all.sumOf { it.signedAmount }
        val openingBalance = closingBalance - signedDelta

        val snapshot = DailySnapshot(
            date = dayKey,
            openingBalance = openingBalance,
            closingBalance = closingBalance,
            totalCredit = totalCredit,
            totalDebit = totalDebit,
            transactionCount = all.size,
        )
        userDoc.collection("dailySnapshots").document(dayKey).set(snapshot).await()
    }

    private fun dayBounds(date: Date): Pair<Date, Date> {
        val start = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
        val end = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.time
        return start to end
    }

    private fun endOfDay(date: Date): Date = dayBounds(date).second

    private fun signFor(type: String): Int = if (type == TYPE_DEBIT) -1 else 1

    private fun userDocRef(uid: String) =
        FirebaseFirestore.getInstance().collection("users").document(uid)

    private fun firestoreUserDoc(): DocumentReference? = currentUid?.let(::userDocRef)

    private fun currentMonthKey(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
    }
}

val Transaction.signedAmount: Double
    get() = if (type == PaisaRepository.TYPE_DEBIT) -amount else amount

val Transaction.isTagged: Boolean
    get() = category.isNotBlank()
