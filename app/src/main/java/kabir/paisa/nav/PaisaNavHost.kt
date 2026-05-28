package kabir.paisa.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kabir.paisa.amount.AmountEntryScreen
import kabir.paisa.amount.AmountScreen
import kabir.paisa.analytics.AnalyticsScreen
import kabir.paisa.auth.AuthScreen
import kabir.paisa.budget.BudgetOverviewScreen
import kabir.paisa.budget.BudgetSetupScreen
import kabir.paisa.common.ui.NavTab
import kabir.paisa.data.AuthRepository
import kabir.paisa.home.HomeScreen
import kabir.paisa.notifications.TaggingScreen
import kabir.paisa.settings.SettingsScreen

object Routes {
    const val Auth = "auth"
    const val Home = "home"
    const val Amount = "amount"
    const val AmountEntry = "amount/entry/{isAdd}"
    fun amountEntry(isAdd: Boolean) = "amount/entry/$isAdd"
    const val Budget = "budget"
    const val BudgetSetup = "budget/setup"
    const val Analytics = "analytics"
    const val Settings = "settings"
    const val Tagging = "tagging"
}

@Composable
fun PaisaNavHost(nav: NavHostController = rememberNavController()) {
    val startDestination = if (AuthRepository.isSignedIn) Routes.Home else Routes.Auth

    fun handleTab(tab: NavTab) {
        val route = when (tab) {
            NavTab.Home -> Routes.Home
            NavTab.Amount -> Routes.Amount
            NavTab.Budget -> Routes.Budget
            NavTab.Analytics -> Routes.Analytics
            NavTab.Settings -> Routes.Settings
        }
        nav.navigate(route) {
            popUpTo(Routes.Home) { inclusive = false; saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.Auth) {
            AuthScreen(onSignedIn = {
                nav.navigate(Routes.Home) { popUpTo(Routes.Auth) { inclusive = true } }
            })
        }
        composable(Routes.Home) {
            HomeScreen(
                onSeeAllTransactions = { nav.navigate(Routes.Amount) },
                onTab = ::handleTab,
            )
        }
        composable(Routes.Amount) {
            AmountScreen(
                onAdd = { nav.navigate(Routes.amountEntry(true)) },
                onSubtract = { nav.navigate(Routes.amountEntry(false)) },
                onTab = ::handleTab,
            )
        }
        composable(Routes.AmountEntry) { backStack ->
            val isAdd = backStack.arguments?.getString("isAdd")?.toBooleanStrictOrNull() ?: false
            AmountEntryScreen(
                initialIsAdd = isAdd,
                onBack = { nav.popBackStack() },
                onConfirmed = { nav.popBackStack() }
            )
        }
        composable(Routes.Budget) {
            BudgetOverviewScreen(
                onEdit = { nav.navigate(Routes.BudgetSetup) },
                onTab = ::handleTab,
            )
        }
        composable(Routes.BudgetSetup) {
            BudgetSetupScreen(onDone = { nav.popBackStack() })
        }
        composable(Routes.Analytics) {
            AnalyticsScreen(onTab = ::handleTab)
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onSignedOut = {
                    nav.navigate(Routes.Auth) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                },
                onTab = ::handleTab,
            )
        }
        composable(Routes.Tagging) {
            TaggingScreen(onBack = { nav.popBackStack() })
        }
    }
}
