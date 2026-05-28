package kabir.paisa.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PaisaLightColorScheme = lightColorScheme(
    primary = PaisaColors.Primary,
    onPrimary = PaisaColors.OnPrimary,
    primaryContainer = PaisaColors.PrimaryContainer,
    onPrimaryContainer = PaisaColors.OnPrimaryContainer,
    secondary = PaisaColors.Secondary,
    onSecondary = PaisaColors.OnSecondary,
    secondaryContainer = PaisaColors.SecondaryContainer,
    onSecondaryContainer = PaisaColors.OnSecondaryContainer,
    tertiary = PaisaColors.Tertiary,
    onTertiary = PaisaColors.OnTertiary,
    tertiaryContainer = PaisaColors.TertiaryContainer,
    onTertiaryContainer = PaisaColors.OnTertiaryContainer,
    error = PaisaColors.Error,
    onError = PaisaColors.OnError,
    errorContainer = PaisaColors.ErrorContainer,
    onErrorContainer = PaisaColors.OnErrorContainer,
    background = PaisaColors.Background,
    onBackground = PaisaColors.OnBackground,
    surface = PaisaColors.Surface,
    onSurface = PaisaColors.OnSurface,
    surfaceVariant = PaisaColors.SurfaceVariant,
    onSurfaceVariant = PaisaColors.OnSurfaceVariant,
    surfaceTint = PaisaColors.SurfaceTint,
    inverseSurface = PaisaColors.InverseSurface,
    inverseOnSurface = PaisaColors.InverseOnSurface,
    inversePrimary = PaisaColors.InversePrimary,
    outline = PaisaColors.Outline,
    outlineVariant = PaisaColors.OutlineVariant,
    surfaceContainerLowest = PaisaColors.SurfaceContainerLowest,
    surfaceContainerLow = PaisaColors.SurfaceContainerLow,
    surfaceContainer = PaisaColors.SurfaceContainer,
    surfaceContainerHigh = PaisaColors.SurfaceContainerHigh,
    surfaceContainerHighest = PaisaColors.SurfaceContainerHighest,
)

@Composable
fun PaisaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = PaisaLightColorScheme // light-only for now per design spec

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PaisaColors.Primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PaisaTypography,
        shapes = PaisaShapes,
        content = content
    )
}
