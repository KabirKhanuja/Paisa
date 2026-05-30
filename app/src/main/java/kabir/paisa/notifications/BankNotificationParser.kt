package kabir.paisa.notifications

import kabir.paisa.data.PaisaRepository

data class ParsedTransaction(
    val amount: Double,
    val type: String,        // PaisaRepository.TYPE_CREDIT / TYPE_DEBIT
    val merchantHint: String?,
    val rawText: String,
)

object BankNotificationParser {

    private val knownBankPackages = setOf(
        "com.csam.icici.bank.imobile",
        "com.snapwork.hdfc",
        "com.axis.mobile",
        "com.sbi.SBIFreedomPlus",
        "com.kotak.android.mob",
        "com.fss.pnb",
    )

    fun looksLikeBank(packageName: String?, text: String): Boolean {
        if (packageName == null) return false
        if (packageName in knownBankPackages) return true
        val l = text.lowercase()
        return ("a/c" in l || "acc" in l || "bank" in l) &&
               ("debited" in l || "credited" in l || "spent" in l || "received" in l || "rs." in l || "inr" in l || "₹" in l)
    }

    private val amountRegex = Regex("""(?:rs\.?|inr|₹)\s*([0-9]+(?:,[0-9]+)*(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
    private val debitWords = listOf("debited", "spent", "withdrawn", "paid", "purchase")
    private val creditWords = listOf("credited", "received", "deposited", "refund")
    private val merchantRegex = Regex("""at\s+([A-Z][A-Za-z0-9 &.'\-]{2,40})""")

    fun parse(text: String): ParsedTransaction? {
        val match = amountRegex.find(text) ?: return null
        val raw = match.groupValues[1].replace(",", "")
        val amount = raw.toDoubleOrNull() ?: return null
        val l = text.lowercase()
        val type = when {
            debitWords.any { it in l } -> PaisaRepository.TYPE_DEBIT
            creditWords.any { it in l } -> PaisaRepository.TYPE_CREDIT
            else -> PaisaRepository.TYPE_DEBIT
        }
        val merchant = merchantRegex.find(text)?.groupValues?.get(1)?.trim()
        return ParsedTransaction(amount, type, merchant, text)
    }
}
