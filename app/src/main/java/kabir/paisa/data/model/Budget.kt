package kabir.paisa.data.model

data class FixedExpense(
    val id: String,
    val name: String,
    val amount: Double,
    val iconName: String = "subscriptions",
)

data class Budget(
    val monthlyIncome: Double = 0.0,
    val spendingCap: Double = 0.0,
    val fixedExpenses: List<FixedExpense> = emptyList(),
) {
    val totalFixed: Double get() = fixedExpenses.sumOf { it.amount }
    val flexBudget: Double get() = (monthlyIncome - totalFixed - investments).coerceAtLeast(0.0)
    val investments: Double get() {
        // Remaining after spending cap and fixed expenses, if a cap is set.
        if (spendingCap <= 0.0) return 0.0
        val afterFixed = monthlyIncome - totalFixed
        return (afterFixed - spendingCap).coerceAtLeast(0.0)
    }
}
