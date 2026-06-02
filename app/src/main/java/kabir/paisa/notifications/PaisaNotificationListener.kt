package kabir.paisa.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kabir.paisa.data.PaisaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PaisaNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        scope.launch {
            runCatching {
                PaisaRepository.addTransaction(
                    amount = parsed.amount,
                    type = parsed.type,
                    category = "",
                    note = merchant,
                    source = PaisaRepository.SOURCE_AUTO,
                )
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
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
