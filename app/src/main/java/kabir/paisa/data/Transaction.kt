package kabir.paisa.data

import com.google.firebase.Timestamp

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val type: String = "",        // "credit" or "debit"
    val category: String = "",
    val note: String = "",
    val source: String = "",      // "auto" or "manual"
    val date: Timestamp = Timestamp.now(),
    val balanceAfter: Double = 0.0
)
