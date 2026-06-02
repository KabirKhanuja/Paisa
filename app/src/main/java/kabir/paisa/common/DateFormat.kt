package kabir.paisa.common

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val timeOnly = SimpleDateFormat("hh:mm a", Locale.getDefault())
private val dayMonth = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
private val dayHeader = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
private val isoDayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)

/** Stable group key for a given timestamp, in the same `yyyy-MM-dd` format used by Firestore. */
fun dayKey(ts: Timestamp): String = isoDayKey.format(ts.toDate())

fun Timestamp.asDate(): Date = toDate()

fun formatTime(ts: Timestamp): String = timeOnly.format(ts.toDate())
fun formatLongDate(ts: Timestamp): String = dayMonth.format(ts.toDate())

fun relativeDayLabel(ts: Timestamp): String {
    val date = ts.toDate()
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { time = date }
    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    if (sameDay) return "Today, ${timeOnly.format(date)}"
    now.add(Calendar.DAY_OF_YEAR, -1)
    val isYesterday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    if (isYesterday) return "Yesterday, ${timeOnly.format(date)}"
    return dayMonth.format(date)
}

fun dayGroupKey(ts: Timestamp): String {
    val date = ts.toDate()
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { time = date }
    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    if (sameDay) return "Today"
    now.add(Calendar.DAY_OF_YEAR, -1)
    val isYesterday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    if (isYesterday) return "Yesterday"
    return dayHeader.format(date)
}
