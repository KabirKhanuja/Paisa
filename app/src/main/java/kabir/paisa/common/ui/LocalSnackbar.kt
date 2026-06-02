package kabir.paisa.common.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * App-wide snackbar dispatcher provided by the NavHost so any screen can
 * surface a message that survives a navigation, e.g. settings → home.
 */
val LocalSnackbar = staticCompositionLocalOf<(String) -> Unit> {
    error("LocalSnackbar not provided")
}
