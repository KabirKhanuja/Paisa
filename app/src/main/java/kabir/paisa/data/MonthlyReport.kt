package kabir.paisa.data

import com.google.firebase.Timestamp

data class MonthlyReport(
    val month: String = "",
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0,
    val costliestDay: String = "",
    val costliestDayAmount: Double = 0.0,
    val avgDailySpend: Double = 0.0,
    val generatedAt: Timestamp = Timestamp.now(),
    val byCategory: Map<String, Double> = emptyMap()
)
