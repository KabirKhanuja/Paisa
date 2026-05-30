package kabir.paisa.data

import com.google.firebase.Timestamp

data class User(
    val name: String = "",
    val email: String = "",
    val bank: String = "",
    val startingBalance: Double = 0.0,
    val currency: String = "INR",
    val createdAt: Timestamp = Timestamp.now()
)
