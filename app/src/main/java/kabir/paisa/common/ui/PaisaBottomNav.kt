package kabir.paisa.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kabir.paisa.ui.theme.PaisaColors

enum class NavTab(val label: String) { Home("Home"), Amount("Amount"), Budget("Budget"), Analytics("Analytics"), Settings("Settings") }

@Composable
fun PaisaBottomNav(
    current: NavTab,
    onSelect: (NavTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaisaColors.SurfaceContainerLowest)
            .border(width = 0.5.dp, color = PaisaColors.OutlineVariant)
            .height(64.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        NavItem(NavTab.Home, current, Icons.Filled.Home, Icons.Outlined.Home, onSelect)
        NavItem(NavTab.Amount, current, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, onSelect)
        NavItem(NavTab.Budget, current, Icons.Filled.Savings, Icons.Outlined.Savings, onSelect)
        NavItem(NavTab.Analytics, current, Icons.Filled.QueryStats, Icons.Outlined.QueryStats, onSelect)
        NavItem(NavTab.Settings, current, Icons.Filled.Settings, Icons.Outlined.Settings, onSelect)
    }
}

@Composable
private fun RowScope.NavItem(
    tab: NavTab,
    current: NavTab,
    filled: ImageVector,
    outlined: ImageVector,
    onSelect: (NavTab) -> Unit,
) {
    val selected = current == tab
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable { onSelect(tab) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(
                    if (selected) PaisaColors.Primary.copy(alpha = 0.06f) else androidx.compose.ui.graphics.Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (selected) filled else outlined,
                contentDescription = tab.label,
                tint = if (selected) PaisaColors.Primary else PaisaColors.Outline
            )
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) PaisaColors.Primary else PaisaColors.Outline
            )
        }
    }
}
