package kabir.paisa.data.model

import java.util.Date
import java.util.UUID

enum class TransactionType { CREDIT, DEBIT }

enum class TransactionSource { AUTO, MANUAL }

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val merchant: String,
    val amount: Double,             // positive value; sign comes from `type`
    val type: TransactionType,
    val source: TransactionSource,
    val categoryId: String? = null, // null = untagged
    val note: String? = null,
    val timestamp: Date = Date(),
    val rawNotificationText: String? = null,
) {
    val signedAmount: Double get() = if (type == TransactionType.DEBIT) -amount else amount
    val isTagged: Boolean get() = categoryId != null
}
