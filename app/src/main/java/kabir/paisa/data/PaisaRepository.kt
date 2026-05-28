package kabir.paisa.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kabir.paisa.data.model.Budget
import kabir.paisa.data.model.FixedExpense
import kabir.paisa.data.model.Transaction
import kabir.paisa.data.model.TransactionSource
import kabir.paisa.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * In-memory single source of truth for the UI. Hooks for Firestore are present
 * but writes are best-effort — UI never blocks on them.
 */
object PaisaRepository {

    private val _transactions = MutableStateFlow(seedTransactions())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _budget = MutableStateFlow(seedBudget())
    val budget: StateFlow<Budget> = _budget.asStateFlow()

    private val _startingBalance = MutableStateFlow(50_000.0)
    val startingBalance: StateFlow<Double> = _startingBalance.asStateFlow()

    val balance: Double
        get() = _startingBalance.value + _transactions.value.sumOf { it.signedAmount }

    fun addTransaction(t: Transaction) {
        _transactions.value = (listOf(t) + _transactions.value).sortedByDescending { it.timestamp }
        pushToFirestore(t)
    }

    fun tagTransaction(id: String, categoryId: String, note: String? = null) {
        _transactions.value = _transactions.value.map {
            if (it.id == id) it.copy(categoryId = categoryId, note = note ?: it.note) else it
        }
        // push update best-effort
        firestoreUserDoc()?.collection("transactions")?.document(id)
            ?.update(mapOf("categoryId" to categoryId, "note" to note))
    }

    fun updateBudget(b: Budget) {
        _budget.value = b
        firestoreUserDoc()?.collection("meta")?.document("budget")?.set(
            mapOf(
                "monthlyIncome" to b.monthlyIncome,
                "spendingCap" to b.spendingCap,
                "fixedExpenses" to b.fixedExpenses.map { e ->
                    mapOf("id" to e.id, "name" to e.name, "amount" to e.amount, "iconName" to e.iconName)
                }
            )
        )
    }

    fun setStartingBalance(amount: Double) {
        _startingBalance.value = amount
    }

    fun untaggedTransactions(): List<Transaction> =
        _transactions.value.filter { !it.isTagged && it.type == TransactionType.DEBIT }

    fun balanceAt(date: Date): Double {
        val cutoff = endOfDay(date)
        return _startingBalance.value + _transactions.value
            .filter { it.timestamp <= cutoff }
            .sumOf { it.signedAmount }
    }

    private fun endOfDay(date: Date): Date {
        val c = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }
        return c.time
    }

    private fun pushToFirestore(t: Transaction) {
        val doc = firestoreUserDoc() ?: return
        doc.collection("transactions").document(t.id).set(
            mapOf(
                "merchant" to t.merchant,
                "amount" to t.amount,
                "type" to t.type.name,
                "source" to t.source.name,
                "categoryId" to t.categoryId,
                "note" to t.note,
                "timestamp" to t.timestamp,
                "rawNotificationText" to t.rawNotificationText,
            )
        )
    }

    private fun firestoreUserDoc() =
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid)
        }

    // ----- seed data so the app shows realistic content immediately -----

    private fun seedTransactions(): List<Transaction> {
        val now = Calendar.getInstance()
        fun at(daysAgo: Int, hour: Int, minute: Int): Date {
            val c = now.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
            c.set(Calendar.HOUR_OF_DAY, hour); c.set(Calendar.MINUTE, minute); c.set(Calendar.SECOND, 0)
            return c.time
        }
        return listOf(
            Transaction(UUID.randomUUID().toString(), "Zomato", 620.0, TransactionType.DEBIT,
                TransactionSource.MANUAL, "food", null, at(0, 12, 45)),
            Transaction(UUID.randomUUID().toString(), "Amazon", 2_100.0, TransactionType.DEBIT,
                TransactionSource.AUTO, "shopping", null, at(0, 10, 30)),
            Transaction(UUID.randomUUID().toString(), "Petrol", 1_500.0, TransactionType.DEBIT,
                TransactionSource.MANUAL, "petrol", null, at(1, 17, 15)),
            Transaction(UUID.randomUUID().toString(), "Electricity Bill", 3_400.0, TransactionType.DEBIT,
                TransactionSource.AUTO, "bills", null, at(1, 9, 0)),
            Transaction(UUID.randomUUID().toString(), "Salary", 85_000.0, TransactionType.CREDIT,
                TransactionSource.AUTO, "salary", null, at(4, 10, 0)),
            Transaction(UUID.randomUUID().toString(), "Axis Bank", 340.0, TransactionType.DEBIT,
                TransactionSource.AUTO, null, null, at(0, 9, 38),
                rawNotificationText = "A/c XX4321 debited ₹340.00. Avl Bal ₹42,240.00"),
            Transaction(UUID.randomUUID().toString(), "Axis Bank", 1_200.0, TransactionType.DEBIT,
                TransactionSource.AUTO, null, null, at(1, 20, 12)),
            Transaction(UUID.randomUUID().toString(), "Axis Bank", 180.0, TransactionType.DEBIT,
                TransactionSource.AUTO, null, null, at(2, 18, 45)),
        ).sortedByDescending { it.timestamp }
    }

    private fun seedBudget() = Budget(
        monthlyIncome = 125_000.0,
        spendingCap = 85_000.0,
        fixedExpenses = listOf(
            FixedExpense("claude", "Claude", 2_251.0, "smart_toy"),
            FixedExpense("music", "Music", 4_800.0, "music_note"),
            FixedExpense("petrol", "Petrol", 600.0, "local_gas_station"),
        )
    )
}
