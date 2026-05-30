package kabir.paisa.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kabir.paisa.common.formatRupees
import kabir.paisa.common.relativeDayLabel
import kabir.paisa.common.ui.CategoryDefs
import kabir.paisa.common.ui.NavTab
import kabir.paisa.common.ui.PaisaBottomNav
import kabir.paisa.common.ui.iconForName
import kabir.paisa.data.PaisaRepository
import kabir.paisa.data.Transaction
import kabir.paisa.ui.theme.PaisaColors
import kabir.paisa.ui.theme.PaisaTextStyles
import java.util.Calendar

@Composable
fun HomeScreen(
    onSeeAllTransactions: () -> Unit,
    onTab: (NavTab) -> Unit,
) {
    val txs by PaisaRepository.transactions.collectAsStateWithLifecycle()
    val budget by PaisaRepository.budget.collectAsStateWithLifecycle()

    val monthSpent = monthSpent(txs)
    val monthCredit = monthCredit(txs)
    val cap = if (budget.spendingCap > 0) budget.spendingCap else 15_000.0
    val leftToSpend = (cap - monthSpent).coerceAtLeast(0.0)

    Scaffold(
        containerColor = PaisaColors.Background,
        bottomBar = { PaisaBottomNav(NavTab.Home, onTab) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            item { HomeHeader() }
            item { HomeHero(balance = PaisaRepository.balance, leftToSpend = leftToSpend, cap = cap, spent = monthSpent) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        color = PaisaColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "See All",
                        color = PaisaColors.Primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onSeeAllTransactions() }
                    )
                }
            }
            items(txs.take(5)) { tx ->
                TransactionRow(tx)
                Spacer(Modifier.height(8.dp))
            }
            item {
                Spacer(Modifier.height(16.dp))
                MonthGlanceCard(credit = monthCredit, debit = monthSpent)
            }
            item {
                Spacer(Modifier.height(16.dp))
                TopCategoryCard(txs = txs, monthlyLimit = 12_000.0)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaisaColors.Primary)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PaisaColors.OnPrimary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("P", color = PaisaColors.OnPrimary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.size(12.dp))
            Text(
                "Paisa",
                color = PaisaColors.OnPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Icon(
            Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = PaisaColors.OnPrimary
        )
    }
}

@Composable
private fun HomeHero(balance: Double, leftToSpend: Double, cap: Double, spent: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaisaColors.Primary)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            "TOTAL BALANCE",
            color = PaisaColors.OnPrimary.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(4.dp))
        Text(
            formatRupees(balance),
            color = PaisaColors.OnPrimary,
            style = PaisaTextStyles.BalanceHero
        )
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "LEFT TO SPEND",
                color = PaisaColors.OnPrimary.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                formatRupees(leftToSpend, withDecimals = false),
                color = PaisaColors.OnPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        val pct = if (cap > 0) (spent / cap).coerceIn(0.0, 1.0).toFloat() else 0f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(androidx.compose.ui.graphics.Color(0xFF22C55E), RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .height(12.dp)
                    .background(androidx.compose.ui.graphics.Color(0xFFEF4444), RoundedCornerShape(999.dp))
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${formatRupees(spent, withDecimals = false)} of ${formatRupees(cap, withDecimals = false)} spent",
                color = PaisaColors.OnPrimary.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall
            )
            Text("Remaining", color = PaisaColors.OnPrimary, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction) {
    val cat = CategoryDefs.byName(tx.category)
    val isCredit = tx.type == PaisaRepository.TYPE_CREDIT
    val displayName = if (tx.note.isNotBlank()) tx.note else if (isCredit) "Credit" else "Debit"
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .border(0.5.dp, PaisaColors.Outline, RoundedCornerShape(14.dp))
            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isCredit) PaisaColors.PrimaryFixedDim else PaisaColors.SurfaceContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    iconForName(cat?.iconName ?: if (isCredit) "payments" else "shopping_bag"),
                    contentDescription = null,
                    tint = if (isCredit) PaisaColors.Primary else PaisaColors.OnSurfaceVariant
                )
            }
            Spacer(Modifier.size(16.dp))
            Column {
                Text(displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = PaisaColors.OnSurface)
                Text(relativeDayLabel(tx.date), style = MaterialTheme.typography.labelSmall, color = PaisaColors.Outline)
            }
        }
        Text(
            (if (isCredit) "+ " else "- ") + formatRupees(tx.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isCredit) PaisaColors.Primary else PaisaColors.Tertiary
        )
    }
}

@Composable
private fun MonthGlanceCard(credit: Double, debit: Double) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .border(0.5.dp, PaisaColors.Outline, RoundedCornerShape(14.dp))
            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Month at a glance", style = MaterialTheme.typography.labelLarge, color = PaisaColors.OnSurfaceVariant)
            Icon(Icons.Filled.Info, null, tint = PaisaColors.Outline, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(0.4f, 0.85f, 0.6f, 0.3f).forEachIndexed { idx, h ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height((64 * h).dp)
                        .background(
                            if (idx == 1) PaisaColors.Primary else PaisaColors.SecondaryContainer,
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Credit", style = MaterialTheme.typography.labelSmall, color = PaisaColors.Outline)
                Text(formatRupees(credit, withDecimals = false), color = PaisaColors.Primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Debit", style = MaterialTheme.typography.labelSmall, color = PaisaColors.Outline)
                Text(formatRupees(debit, withDecimals = false), color = PaisaColors.Tertiary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TopCategoryCard(txs: List<Transaction>, monthlyLimit: Double) {
    val spendByCategory = txs.filter { it.type == PaisaRepository.TYPE_DEBIT && it.category.isNotBlank() }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
    val topEntry = spendByCategory.maxByOrNull { it.value } ?: return
    val cat = CategoryDefs.byName(topEntry.key) ?: CategoryDefs.byName("Other") ?: return
    val pct = (topEntry.value / monthlyLimit).coerceIn(0.0, 1.0).toFloat()

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .border(0.5.dp, PaisaColors.Outline, RoundedCornerShape(14.dp))
            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Top Category", color = PaisaColors.OnSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            Text("${(pct * 100).toInt()}% of budget", color = PaisaColors.Primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PaisaColors.PrimaryFixedDim, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(iconForName(cat.iconName), null, tint = PaisaColors.Primary) }
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cat.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Monthly Limit: ${formatRupees(monthlyLimit, withDecimals = false)}", style = MaterialTheme.typography.labelSmall, color = PaisaColors.Outline)
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(PaisaColors.SurfaceContainerHigh, RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .height(8.dp)
                    .background(PaisaColors.Primary, RoundedCornerShape(999.dp))
            )
        }
    }
}

private fun monthSpent(txs: List<Transaction>): Double {
    val now = Calendar.getInstance()
    val month = now.get(Calendar.MONTH); val year = now.get(Calendar.YEAR)
    return txs.filter {
        val c = Calendar.getInstance().apply { time = it.date.toDate() }
        c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year && it.type == PaisaRepository.TYPE_DEBIT
    }.sumOf { it.amount }
}

private fun monthCredit(txs: List<Transaction>): Double {
    val now = Calendar.getInstance()
    val month = now.get(Calendar.MONTH); val year = now.get(Calendar.YEAR)
    return txs.filter {
        val c = Calendar.getInstance().apply { time = it.date.toDate() }
        c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year && it.type == PaisaRepository.TYPE_CREDIT
    }.sumOf { it.amount }
}
