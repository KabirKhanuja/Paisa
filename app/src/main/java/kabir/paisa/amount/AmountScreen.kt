package kabir.paisa.amount

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kabir.paisa.common.dayGroupKey
import kabir.paisa.common.formatRupees
import kabir.paisa.common.formatTime
import kabir.paisa.common.ui.CategoryDefs
import kabir.paisa.common.ui.NavTab
import kabir.paisa.common.ui.PaisaBottomNav
import kabir.paisa.common.ui.iconForName
import kabir.paisa.data.PaisaRepository
import kabir.paisa.data.Transaction
import kabir.paisa.ui.theme.PaisaColors
import kabir.paisa.ui.theme.PaisaTextStyles

@Composable
fun AmountScreen(
    onAdd: () -> Unit,
    onSubtract: () -> Unit,
    onTab: (NavTab) -> Unit,
) {
    val txs by PaisaRepository.transactions.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PaisaColors.Background,
        bottomBar = { PaisaBottomNav(NavTab.Amount, onTab) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PaisaColors.Primary)
                        .padding(top = 40.dp, bottom = 48.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "TOTAL BALANCE",
                        color = PaisaColors.OnPrimaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        formatRupees(PaisaRepository.balance),
                        style = PaisaTextStyles.AmountDisplay,
                        color = PaisaColors.OnPrimary
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onAdd,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PaisaColors.SurfaceContainerLowest,
                                contentColor = PaisaColors.Primary
                            )
                        ) { Text("Add money", fontWeight = FontWeight.Bold) }
                        OutlinedButton(
                            onClick = onSubtract,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(999.dp),
                        ) {
                            Text("Subtract", color = PaisaColors.OnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            val grouped = txs.groupBy { dayGroupKey(it.date) }
            grouped.forEach { (group, items) ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(group, color = PaisaColors.Outline, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.size(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(0.5.dp)
                                .background(PaisaColors.OutlineVariant)
                        )
                    }
                }
                items(items) { tx ->
                    AmountTxRow(tx)
                    Spacer(Modifier.height(8.dp))
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun AmountTxRow(tx: Transaction) {
    val cat = CategoryDefs.byName(tx.category)
    val isCredit = tx.type == PaisaRepository.TYPE_CREDIT
    val displayName = if (tx.note.isNotBlank()) tx.note else if (isCredit) "Credit" else "Debit"
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
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isCredit) PaisaColors.PrimaryFixedDim else PaisaColors.SecondaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(999.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    iconForName(cat?.iconName ?: if (isCredit) "payments" else "shopping_bag"),
                    null,
                    tint = if (isCredit) PaisaColors.Primary else PaisaColors.Secondary
                )
            }
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(8.dp))
                    SourceBadge(tx.source)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (cat != null) {
                        Box(
                            modifier = Modifier
                                .background(PaisaColors.SecondaryFixed, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(cat.name, color = PaisaColors.OnSecondaryContainer, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.size(8.dp))
                    }
                    Text(formatTime(tx.date), color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(
            (if (isCredit) "+" else "-") + formatRupees(tx.amount),
            color = if (isCredit) PaisaColors.Primary else PaisaColors.Tertiary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SourceBadge(source: String) {
    val isAuto = source == PaisaRepository.SOURCE_AUTO
    val (bg, fg, label) = if (isAuto)
        Triple(PaisaColors.PrimaryFixed, PaisaColors.OnPrimaryFixed, "AUTO")
    else
        Triple(PaisaColors.SurfaceContainerHigh, PaisaColors.Outline, "MANUAL")
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}
