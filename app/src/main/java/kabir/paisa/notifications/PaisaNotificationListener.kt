package kabir.paisa.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kabir.paisa.data.PaisaRepository

class PaisaNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val combined = listOf(title, text, bigText).filter { it.isNotBlank() }.joinToString(" — ")
        val pkg = sbn.packageName

        if (!BankNotificationParser.looksLikeBank(pkg, combined)) return
        val parsed = BankNotificationParser.parse(combined) ?: return

        val merchant = parsed.merchantHint ?: bankNameFromPackage(pkg) ?: "Bank"
        PaisaRepository.addTransaction(
            amount = parsed.amount,
            type = parsed.type,
            category = "",            // untagged — surfaces in tagging flow
            note = merchant,
            source = PaisaRepository.SOURCE_AUTO,
        )
    }

    private fun bankNameFromPackage(pkg: String?): String? = when (pkg) {
        "com.snapwork.hdfc" -> "HDFC Bank"
        "com.axis.mobile" -> "Axis Bank"
        "com.csam.icici.bank.imobile" -> "ICICI Bank"
        "com.sbi.SBIFreedomPlus" -> "SBI"
        "com.kotak.android.mob" -> "Kotak Bank"
        "com.fss.pnb" -> "PNB"
        else -> null
    }
}
