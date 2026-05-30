package kabir.paisa.data

data class FixedExpense(
    val name: String = "",
    val amount: Double = 0.0,
    val icon: String = ""
)

data class Budget(
    val salary: Double = 0.0,
    val spendingCap: Double = 0.0,
    val investmentTarget: Double = 0.0,
    val flexBudget: Double = 0.0,
    val fixedExpenses: List<FixedExpense> = emptyList()
)
