package kabir.paisa.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kabir.paisa.common.formatRupees
import kabir.paisa.common.ui.EmptyState
import kabir.paisa.common.ui.NavTab
import kabir.paisa.common.ui.PaisaBottomNav
import kabir.paisa.data.PaisaRepository
import kabir.paisa.ui.theme.PaisaColors
import kabir.paisa.ui.theme.PaisaTextStyles
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(onTab: (NavTab) -> Unit) {
    // One-shot reload on entry so analytics is fresh on every tab switch,
    // independent of the always-on snapshot listener.
    LaunchedEffect(Unit) { PaisaRepository.refreshAnalytics() }

    val txs by PaisaRepository.transactions.collectAsStateWithLifecycle()

    val now = Calendar.getInstance()
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(now.time)
    val monthTxs = txs.filter {
        val c = Calendar.getInstance().apply { time = it.date.toDate() }
        c.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                c.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                it.type == PaisaRepository.TYPE_DEBIT
    }
    val monthSpent = monthTxs.sumOf { it.amount }
    val today = now.get(Calendar.DAY_OF_MONTH)
    val balanceToday = PaisaRepository.balanceAt(now.time)

    val byDay = monthTxs.groupBy {
        Calendar.getInstance().apply { time = it.date.toDate() }.get(Calendar.DAY_OF_MONTH)
    }.mapValues { it.value.sumOf { tx -> tx.amount } }
    val costliest = byDay.maxByOrNull { it.value }
    val avgPerDay = if (today > 0) monthSpent / today else 0.0

    val byCategory = monthTxs.filter { it.category.isNotBlank() }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .entries.sortedByDescending { it.value }

    Scaffold(
        containerColor = PaisaColors.Background,
        bottomBar = { PaisaBottomNav(NavTab.Analytics, onTab) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PaisaColors.Primary)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(monthLabel, color = PaisaColors.OnPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("You've spent ${formatRupees(monthSpent, withDecimals = false)} this month", color = PaisaColors.OnPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                    }
                    Box(
                        modifier = Modifier.size(40.dp).background(PaisaColors.OnPrimary.copy(alpha = 0.1f), RoundedCornerShape(999.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Notifications, null, tint = PaisaColors.OnPrimary)
                    }
                }
            }
            if (monthTxs.isEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    EmptyState(
                        icon = Icons.Filled.QueryStats,
                        title = "No spending this month yet",
                        subtitle = "Once transactions come in, you'll see your calendar, costliest day and category breakdown here.",
                    )
                }
                return@LazyColumn
            }
            item {
                Spacer(Modifier.height(16.dp))
                SpendCalendar(byDay = byDay, today = today, monthlyMax = byDay.values.maxOrNull() ?: 1.0)
            }
            item {
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                        .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("BALANCE TODAY", color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(formatRupees(balanceToday), style = PaisaTextStyles.AmountDisplay, color = PaisaColors.OnSurface, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                            .padding(24.dp)
                    ) {
                        Text("Costliest Day", color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
                        if (costliest != null) {
                            val label = SimpleDateFormat("MMM d", Locale.getDefault()).format(dateOfDay(costliest.key))
                            Text(label, style = MaterialTheme.typography.titleLarge, color = PaisaColors.OnSurface)
                            Text(formatRupees(costliest.value, withDecimals = false), color = PaisaColors.Primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        } else {
                            Text("—", color = PaisaColors.Outline)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                            .padding(24.dp)
                    ) {
                        Text("Avg per Day", color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
                        Text(formatRupees(avgPerDay, withDecimals = false), style = MaterialTheme.typography.titleLarge, color = PaisaColors.OnSurface)
                        Text("Consistent", color = PaisaColors.OnSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                        .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text("WHERE IT WENT", color = PaisaColors.OnSurfaceVariant, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(16.dp))
                    val max = byCategory.firstOrNull()?.value ?: 1.0
                    byCategory.forEachIndexed { idx, (catName, amount) ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(catName, color = PaisaColors.OnSurface, style = MaterialTheme.typography.labelLarge)
                                Text(formatRupees(amount, withDecimals = false), color = PaisaColors.OnSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                            }
                            Spacer(Modifier.height(4.dp))
                            val pct = (amount / max).coerceIn(0.0, 1.0).toFloat()
                            val tint = PaisaColors.Primary.copy(alpha = 1f - (idx * 0.2f).coerceAtMost(0.7f))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(PaisaColors.SurfaceContainer, RoundedCornerShape(999.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pct)
                                        .height(8.dp)
                                        .background(tint, RoundedCornerShape(999.dp))
                                )
                            }
                        }
                    }
                    if (byCategory.isEmpty()) {
                        Text("No tagged transactions yet.", color = PaisaColors.Outline, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: export report */ },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PaisaColors.Primary,
                        contentColor = PaisaColors.OnPrimary
                    )
                ) {
                    Icon(Icons.Filled.Description, null)
                    Spacer(Modifier.size(8.dp))
                    Text("Generate report", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun dateOfDay(day: Int): Date {
    val c = Calendar.getInstance()
    c.set(Calendar.DAY_OF_MONTH, day)
    return c.time
}

@Composable
private fun SpendCalendar(byDay: Map<Int, Double>, today: Int, monthlyMax: Double) {
    val cal = Calendar.getInstance()
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text("SPEND CALENDAR", color = PaisaColors.OnSurfaceVariant, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp, top = 8.dp))
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S","M","T","W","T","F","S").forEach {
                Text(it, modifier = Modifier.weight(1f), color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Spacer(Modifier.height(4.dp))
        val cells = firstDow + daysInMonth
        val rows = (cells + 6) / 7
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                for (c in 0 until 7) {
                    val idx = r * 7 + c
                    val day = idx - firstDow + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day in 1..daysInMonth) {
                            val spent = byDay[day] ?: 0.0
                            val frac = if (monthlyMax > 0) (spent / monthlyMax).toFloat() else 0f
                            val bg = if (spent == 0.0) PaisaColors.SurfaceContainer else PaisaColors.Primary.copy(alpha = (0.1f + frac * 0.6f).coerceAtMost(0.9f))
                            val isToday = day == today
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(bg, RoundedCornerShape(4.dp))
                                    .border(
                                        width = if (isToday) 2.dp else 0.dp,
                                        color = if (isToday) PaisaColors.Primary else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isToday) PaisaColors.Primary else PaisaColors.OnSurface,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
