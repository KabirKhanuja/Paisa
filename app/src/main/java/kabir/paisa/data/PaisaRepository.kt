package kabir.paisa.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * In-memory single source of truth for the UI, with best-effort Firestore write-through.
 * All models are Firestore-schema models — no domain/UI mapping layer.
 */
object PaisaRepository {

    const val TYPE_CREDIT = "credit"
    const val TYPE_DEBIT = "debit"
    const val SOURCE_AUTO = "auto"
    const val SOURCE_MANUAL = "manual"

    private val _transactions = MutableStateFlow(seedTransactions())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _budget = MutableStateFlow(seedBudget())
    val budget: StateFlow<Budget> = _budget.asStateFlow()

    private val _startingBalance = MutableStateFlow(50_000.0)
    val startingBalance: StateFlow<Double> = _startingBalance.asStateFlow()

    val balance: Double
        get() = _startingBalance.value + _transactions.value.sumOf { it.signedAmount }

    /** Append a new transaction, computing [Transaction.balanceAfter] from current state. */
    fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        note: String,
        source: String,
        date: Timestamp = Timestamp.now(),
        id: String = UUID.randomUUID().toString(),
    ): Transaction {
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
        _transactions.value = (listOf(tx) + _transactions.value).sortedByDescending { it.date.seconds }
        pushTransaction(tx)
        return tx
    }

    fun tagTransaction(id: String, category: String, note: String? = null) {
        _transactions.value = _transactions.value.map {
            if (it.id == id) it.copy(category = category, note = note ?: it.note) else it
        }
        val updates = mutableMapOf<String, Any>("category" to category)
        if (note != null) updates["note"] = note
        firestoreUserDoc()?.collection("transactions")?.document(id)?.update(updates)
    }

    fun updateBudget(b: Budget) {
        _budget.value = b
        firestoreUserDoc()?.collection("budget")?.document(currentMonthKey())?.set(
            mapOf(
                "salary" to b.salary,
                "spendingCap" to b.spendingCap,
                "investmentTarget" to b.investmentTarget,
                "flexBudget" to b.flexBudget,
                "fixedExpenses" to b.fixedExpenses.map { e ->
                    mapOf("name" to e.name, "amount" to e.amount, "icon" to e.icon)
                }
            )
        )
    }

    fun setStartingBalance(amount: Double) { _startingBalance.value = amount }

    fun untaggedDebits(): List<Transaction> =
        _transactions.value.filter { it.category.isBlank() && it.type == TYPE_DEBIT }

    fun balanceAt(date: Date): Double {
        val cutoff = endOfDay(date).time / 1000
        return _startingBalance.value + _transactions.value
            .filter { it.date.seconds <= cutoff }
            .sumOf { it.signedAmount }
    }

    private fun endOfDay(date: Date): Date {
        val c = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }
        return c.time
    }

    private fun signFor(type: String): Int = if (type == TYPE_DEBIT) -1 else 1

    private fun pushTransaction(tx: Transaction) {
        val doc = firestoreUserDoc() ?: return
        doc.collection("transactions").document(tx.id).set(tx)
    }

    private fun firestoreUserDoc() =
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid)
        }

    private fun currentMonthKey(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
    }

    // ----- seed data so the app shows realistic content immediately -----

    private fun seedTransactions(): List<Transaction> {
        val now = Calendar.getInstance()
        fun at(daysAgo: Int, hour: Int, minute: Int): Timestamp {
            val c = now.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
            c.set(Calendar.HOUR_OF_DAY, hour); c.set(Calendar.MINUTE, minute); c.set(Calendar.SECOND, 0)
            return Timestamp(c.time)
        }
        fun mk(amount: Double, type: String, category: String, note: String, source: String, ts: Timestamp) =
            Transaction(UUID.randomUUID().toString(), amount, type, category, note, source, ts, balanceAfter = 0.0)

        return listOf(
            mk(620.0, TYPE_DEBIT, "Food", "Zomato", SOURCE_MANUAL, at(0, 12, 45)),
            mk(2_100.0, TYPE_DEBIT, "Shopping", "Amazon", SOURCE_AUTO, at(0, 10, 30)),
            mk(1_500.0, TYPE_DEBIT, "Petrol", "Petrol", SOURCE_MANUAL, at(1, 17, 15)),
            mk(3_400.0, TYPE_DEBIT, "Bills", "Electricity Bill", SOURCE_AUTO, at(1, 9, 0)),
            mk(85_000.0, TYPE_CREDIT, "Income", "Salary", SOURCE_AUTO, at(4, 10, 0)),
            mk(340.0, TYPE_DEBIT, "", "Axis Bank", SOURCE_AUTO, at(0, 9, 38)),
            mk(1_200.0, TYPE_DEBIT, "", "Axis Bank", SOURCE_AUTO, at(1, 20, 12)),
            mk(180.0, TYPE_DEBIT, "", "Axis Bank", SOURCE_AUTO, at(2, 18, 45)),
        ).sortedByDescending { it.date.seconds }
    }

    private fun seedBudget(): Budget {
        val salary = 125_000.0
        val cap = 85_000.0
        val fixed = listOf(
            FixedExpense("Claude", 2_251.0, "robot"),
            FixedExpense("Music", 4_800.0, "music"),
            FixedExpense("Petrol", 600.0, "car"),
        )
        val totalFixed = fixed.sumOf { it.amount }
        val investment = (salary - totalFixed - cap).coerceAtLeast(0.0)
        return Budget(
            salary = salary,
            spendingCap = cap,
            investmentTarget = investment,
            flexBudget = cap,
            fixedExpenses = fixed,
        )
    }
}

val Transaction.signedAmount: Double
    get() = if (type == PaisaRepository.TYPE_DEBIT) -amount else amount

val Transaction.isTagged: Boolean
    get() = category.isNotBlank()
