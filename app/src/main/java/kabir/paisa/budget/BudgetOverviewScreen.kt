package kabir.paisa.budget

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kabir.paisa.common.formatRupees
import kabir.paisa.common.ui.NavTab
import kabir.paisa.common.ui.PaisaBottomNav
import kabir.paisa.common.ui.iconForName
import kabir.paisa.data.PaisaRepository
import kabir.paisa.data.model.FixedExpense
import kabir.paisa.ui.theme.PaisaColors

@Composable
fun BudgetOverviewScreen(
    onEdit: () -> Unit,
    onTab: (NavTab) -> Unit,
) {
    val budget by PaisaRepository.budget.collectAsStateWithLifecycle()

    val investments = budget.investments
    val fixed = budget.totalFixed
    val flex = budget.spendingCap.coerceAtLeast(0.0)
    val total = (investments + fixed + flex).coerceAtLeast(1.0)

    Scaffold(
        containerColor = PaisaColors.Background,
        bottomBar = { PaisaBottomNav(NavTab.Budget, onTab) }
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
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccountBalanceWallet, null, tint = PaisaColors.OnPrimary)
                        Spacer(Modifier.size(8.dp))
                        Text("Budget Overview", style = MaterialTheme.typography.titleLarge, color = PaisaColors.OnPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.MoreVert, null, tint = PaisaColors.OnPrimary)
                    }
                }
            }
            item {
                // Investment locked pill
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .background(PaisaColors.PrimaryContainer, RoundedCornerShape(999.dp))
                            .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(999.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lock, null, tint = PaisaColors.OnPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(
                            "${formatRupees(investments, withDecimals = false)} Investment Locked",
                            color = PaisaColors.OnPrimary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .fillMaxWidth()
                        .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                        .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Donut(
                            modifier = Modifier.size(160.dp),
                            slices = listOf(
                                PaisaColors.Primary to (investments / total).toFloat(),
                                PaisaColors.Secondary to (fixed / total).toFloat(),
                                PaisaColors.SecondaryFixed to (flex / total).toFloat(),
                            ),
                            centerLabel = "Total",
                            centerValue = formatRupees(total, withDecimals = false)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            LegendRow(PaisaColors.Primary, "Investments", "${pct(investments, total)}%")
                            Spacer(Modifier.height(8.dp))
                            LegendRow(PaisaColors.Secondary, "Fixed Costs", "${pct(fixed, total)}%")
                            Spacer(Modifier.height(8.dp))
                            LegendRow(PaisaColors.SecondaryFixed, "Flex Budget", "${pct(flex, total)}%")
                        }
                    }
                }
            }
            item {
                // Flex progress
                val txs by PaisaRepository.transactions.collectAsStateWithLifecycle()
                val monthSpent = txs.filter { it.type == kabir.paisa.data.model.TransactionType.DEBIT }.sumOf { it.amount }
                val ratio = if (flex > 0) (monthSpent / flex).coerceIn(0.0, 1.0).toFloat() else 0f
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                        .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("Flex Budget Progress", color = PaisaColors.Outline, style = MaterialTheme.typography.labelLarge)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(formatRupees(monthSpent, withDecimals = false), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(" / ${formatRupees(flex, withDecimals = false)}", color = PaisaColors.Outline, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(PaisaColors.SecondaryContainer, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${(ratio * 100).toInt()}% Allocated",
                                color = PaisaColors.OnSecondaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(PaisaColors.SurfaceContainerHigh, RoundedCornerShape(999.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .height(12.dp)
                                .background(PaisaColors.PrimaryContainer, RoundedCornerShape(999.dp))
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fixed Expenses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Edit", color = PaisaColors.Primary, modifier = Modifier.clickable { onEdit() })
                }
                Spacer(Modifier.height(12.dp))
            }
            items(budget.fixedExpenses) { e ->
                FixedExpenseRow(e)
                Spacer(Modifier.height(12.dp))
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

private fun pct(part: Double, whole: Double): Int =
    if (whole <= 0) 0 else ((part / whole) * 100).toInt()

@Composable
private fun LegendRow(color: Color, label: String, pct: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(999.dp)))
            Spacer(Modifier.size(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(pct, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Donut(
    modifier: Modifier,
    slices: List<Pair<Color, Float>>,
    centerLabel: String,
    centerValue: String,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 18f
            val padding = stroke / 2
            val size = Size(this.size.width - stroke, this.size.height - stroke)
            val offset = Offset(padding, padding)
            var start = -90f
            slices.forEach { (color, frac) ->
                val sweep = 360f * frac
                drawArc(
                    color = color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = offset,
                    size = size,
                    style = Stroke(width = stroke)
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel.uppercase(), color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
            Text(centerValue, color = PaisaColors.OnSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FixedExpenseRow(e: FixedExpense) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(PaisaColors.SurfaceContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconForName(e.iconName), null, tint = PaisaColors.Primary)
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text(e.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text("Monthly", color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
            }
        }
        Text(formatRupees(e.amount, withDecimals = false), color = PaisaColors.Error, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
