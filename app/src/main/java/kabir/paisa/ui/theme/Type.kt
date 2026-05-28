package kabir.paisa.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Work Sans not bundled; system sans-serif renders very close on Android.
private val WorkSans = FontFamily.SansSerif

val PaisaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.02).em
    ),
    headlineLarge = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.02).em
    ),
    headlineMedium = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.01).em
    ),
    headlineSmall = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.01.em
    ),
    labelMedium = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.Medium,
        fontSize = 10.sp, lineHeight = 14.sp
    ),
)

object PaisaTextStyles {
    val AmountDisplay = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.02).em
    )
    val BalanceHero = TextStyle(
        fontFamily = WorkSans, fontWeight = FontWeight.Bold,
        fontSize = 56.sp, lineHeight = 56.sp, letterSpacing = (-0.02).em
    )
}
