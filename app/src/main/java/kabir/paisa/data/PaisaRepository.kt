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
     * Persist a new transaction to Firestore, await the write, then recompute
     * that day's [DailySnapshot] doc.
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
        recomputeDailySnapshot(userDoc, tx.date.toDate(), includeTx = tx)
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
     * Delete one transaction. Recomputes that day's snapshot from the remaining
     * transactions, then re-fetches the full list and balance from Firestore.
     */
    suspend fun deleteTransaction(transactionId: String) {
        val userDoc = firestoreUserDoc()
            ?: error("Cannot delete transaction: no signed-in user.")

        // Resolve the transaction so we know which day's snapshot to recompute.
        val cached = _transactions.value.firstOrNull { it.id == transactionId }
        val tx = cached ?: userDoc.collection("transactions").document(transactionId)
            .get().await().toObject(Transaction::class.java)?.copy(id = transactionId)

        userDoc.collection("transactions").document(transactionId).delete().await()

        if (tx != null) {
            recomputeDailySnapshot(userDoc, tx.date.toDate())
        }
        refreshAnalytics()
    }

    /**
     * Delete every transaction on the given calendar day, then delete its
     * snapshot doc. Date string is `yyyy-MM-dd`.
     */
    suspend fun deleteTransactionsByDate(dateString: String) {
        val userDoc = firestoreUserDoc()
            ?: error("Cannot delete: no signed-in user.")
        val date = dayKeyFmt.parse(dateString)
            ?: error("Invalid date: $dateString (expected yyyy-MM-dd)")
        val (start, end) = dayBounds(date)

        val docs = userDoc.collection("transactions")
            .whereGreaterThanOrEqualTo("date", Timestamp(start))
            .whereLessThan("date", Timestamp(end))
            .get().await()

        val batch = FirebaseFirestore.getInstance().batch()
        docs.forEach { batch.delete(it.reference) }
        batch.commit().await()

        userDoc.collection("dailySnapshots").document(dateString).delete().await()
        refreshAnalytics()
    }

    /**
     * Wipe every transaction, daily snapshot and monthly report under this
     * user, and reset startingBalance to 0.
     */
    suspend fun clearAllData() {
        val userDoc = firestoreUserDoc()
            ?: error("Cannot clear data: no signed-in user.")
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        userDoc.collection("transactions").get().await()
            .forEach { batch.delete(it.reference) }
        userDoc.collection("dailySnapshots").get().await()
            .forEach { batch.delete(it.reference) }
        userDoc.collection("monthlyReports").get().await()
            .forEach { batch.delete(it.reference) }
        batch.commit().await()

        userDoc.update("startingBalance", 0.0).await()
        refreshAnalytics()
    }

    /**
     * One-shot reload of transactions, budget and starting balance from
     * Firestore. Snapshot listeners already keep state live, but call this
     * after a destructive op to guarantee fresh totals.
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
        val userSnap = userDoc.get().await()
        _startingBalance.value = userSnap.getDouble("startingBalance") ?: 0.0
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

    /**
     * Compute the snapshot for [dayDate] from scratch by querying Firestore.
     * Pass [includeTx] when called immediately after a write whose server
     * round-trip may not yet reflect in subsequent queries. If the day ends
     * up with zero transactions, the snapshot doc is deleted instead.
     *
     * `closingBalance` is computed as `startingBalance + signed_sum(all txs <= dayEnd)`
     * — meaning it doesn't depend on any individual tx's stale `balanceAfter`.
     */
    private suspend fun recomputeDailySnapshot(
        userDoc: DocumentReference,
        dayDate: Date,
        includeTx: Transaction? = null,
    ) {
        val dayKey = dayKeyFmt.format(dayDate)
        val (dayStart, dayEnd) = dayBounds(dayDate)

        val dayQuery = userDoc.collection("transactions")
            .whereGreaterThanOrEqualTo("date", Timestamp(dayStart))
            .whereLessThanOrEqualTo("date", Timestamp(dayEnd))
            .get().await()
        val fromServer = dayQuery.documents.mapNotNull { doc ->
            doc.toObject(Transaction::class.java)?.copy(id = doc.id)
        }
        val all = if (includeTx != null) (fromServer + includeTx).distinctBy { it.id } else fromServer

        val snapshotsRef = userDoc.collection("dailySnapshots").document(dayKey)
        if (all.isEmpty()) {
            snapshotsRef.delete().await()
            return
        }

        // Opening balance = startingBalance + signed sum of all txs strictly before this day.
        val priorQuery = userDoc.collection("transactions")
            .whereLessThan("date", Timestamp(dayStart))
            .get().await()
        val priorSignedSum = priorQuery.documents.mapNotNull { doc ->
            doc.toObject(Transaction::class.java)
        }.sumOf { signFor(it.type) * it.amount }
        val openingBalance = _startingBalance.value + priorSignedSum

        val totalCredit = all.filter { it.type == TYPE_CREDIT }.sumOf { it.amount }
        val totalDebit = all.filter { it.type == TYPE_DEBIT }.sumOf { it.amount }
        val signedDelta = all.sumOf { signFor(it.type) * it.amount }
        val closingBalance = openingBalance + signedDelta

        snapshotsRef.set(
            DailySnapshot(
                date = dayKey,
                openingBalance = openingBalance,
                closingBalance = closingBalance,
                totalCredit = totalCredit,
                totalDebit = totalDebit,
                transactionCount = all.size,
            )
        ).await()
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
