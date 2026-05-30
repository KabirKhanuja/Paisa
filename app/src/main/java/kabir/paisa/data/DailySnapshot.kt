package kabir.paisa.data

data class DailySnapshot(
    val date: String = "",
    val openingBalance: Double = 0.0,
    val closingBalance: Double = 0.0,
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0,
    val transactionCount: Int = 0
)
