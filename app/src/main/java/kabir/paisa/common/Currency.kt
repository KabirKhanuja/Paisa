package kabir.paisa.common

import java.text.NumberFormat
import java.util.Locale

private val INR: NumberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

fun formatRupees(value: Double, withDecimals: Boolean = true): String {
    INR.minimumFractionDigits = if (withDecimals) 2 else 0
    INR.maximumFractionDigits = if (withDecimals) 2 else 0
    return "₹" + INR.format(value)
}

fun formatRupeesSigned(value: Double, withDecimals: Boolean = true): String {
    val sign = if (value < 0) "-" else "+"
    return sign + formatRupees(kotlin.math.abs(value), withDecimals)
}
