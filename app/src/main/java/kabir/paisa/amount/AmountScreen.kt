package kabir.paisa.amount

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kabir.paisa.common.dayGroupKey
import kabir.paisa.common.dayKey
import kabir.paisa.common.formatRupees
import kabir.paisa.common.formatTime
import kabir.paisa.common.ui.CategoryDefs
import kabir.paisa.common.ui.EmptyState
import kabir.paisa.common.ui.NavTab
import kabir.paisa.common.ui.PaisaBottomNav
import kabir.paisa.common.ui.iconForName
import kabir.paisa.data.PaisaRepository
import kabir.paisa.data.Transaction
import kabir.paisa.ui.theme.PaisaColors
import kabir.paisa.ui.theme.PaisaTextStyles
import kotlinx.coroutines.launch

@Composable
fun AmountScreen(
    onAdd: () -> Unit,
    onSubtract: () -> Unit,
    onTab: (NavTab) -> Unit,
) {
    val txs by PaisaRepository.transactions.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var txPendingDelete by remember { mutableStateOf<Transaction?>(null) }
    var dayPendingDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // dateKey to displayLabel

    Scaffold(
        containerColor = PaisaColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                AmountActionBar(onAdd = onAdd, onSubtract = onSubtract)
                PaisaBottomNav(NavTab.Amount, onTab)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PaisaColors.Primary)
                        .statusBarsPadding()
                        .padding(top = 24.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
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
                }
            }

            if (txs.isEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    EmptyState(
                        icon = Icons.Filled.Receipt,
                        title = "No transactions yet",
                        subtitle = "Tap “Add money” or “Subtract” above to log your first one.",
                    )
                }
            } else {
                // Group by yyyy-MM-dd so we have the exact key Firestore wants for
                // deletes; the display label comes from the first tx in the group.
                val grouped = txs.groupBy { dayKey(it.date) }
                    .toSortedMap(reverseOrder())
                grouped.forEach { (dateKey, dayTxs) ->
                    val label = dayGroupKey(dayTxs.first().date)
                    item {
                        DayGroupHeader(
                            label = label,
                            onClearDay = { dayPendingDelete = dateKey to label },
                        )
                    }
                    items(dayTxs) { tx ->
                        AmountTxRow(tx, onLongPress = { txPendingDelete = tx })
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    txPendingDelete?.let { tx ->
        DeleteTransactionDialog(
            tx = tx,
            onConfirm = {
                txPendingDelete = null
                scope.launch {
                    runCatching { PaisaRepository.deleteTransaction(tx.id) }
                }
            },
            onDismiss = { txPendingDelete = null },
        )
    }

    dayPendingDelete?.let { (dateKey, label) ->
        DeleteDayDialog(
            label = label,
            onConfirm = {
                dayPendingDelete = null
                scope.launch {
                    runCatching { PaisaRepository.deleteTransactionsByDate(dateKey) }
                }
            },
            onDismiss = { dayPendingDelete = null },
        )
    }
}

@Composable
private fun AmountActionBar(onAdd: () -> Unit, onSubtract: () -> Unit) {
    // Sits in Scaffold.bottomBar above PaisaBottomNav (which owns its own
    // navigationBarsPadding). Background extends behind the buttons; the
    // 0.5dp top hairline separates it from the scrolling list above.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaisaColors.Primary),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(PaisaColors.OutlineVariant)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = onAdd,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaisaColors.SurfaceContainerLowest,
                    contentColor = PaisaColors.Primary,
                ),
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

@Composable
private fun DayGroupHeader(label: String, onClearDay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = PaisaColors.Outline, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(PaisaColors.OutlineVariant)
        )
        IconButton(
            onClick = onClearDay,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                Icons.Filled.DeleteOutline,
                contentDescription = "Clear day",
                tint = PaisaColors.Outline,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AmountTxRow(tx: Transaction, onLongPress: () -> Unit) {
    val cat = CategoryDefs.byName(tx.category)
    val isCredit = tx.type == PaisaRepository.TYPE_CREDIT
    val displayName = if (tx.note.isNotBlank()) tx.note else if (isCredit) "Credit" else "Debit"
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress,
            )
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

@Composable
private fun DeleteTransactionDialog(
    tx: Transaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isCredit = tx.type == PaisaRepository.TYPE_CREDIT
    val signed = (if (isCredit) "+" else "-") + formatRupees(tx.amount)
    val name = if (tx.note.isNotBlank()) tx.note else if (isCredit) "Credit" else "Debit"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete transaction?") },
        text = { Text("$name — $signed\nThis cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete transaction", color = PaisaColors.Tertiary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = PaisaColors.SurfaceContainerLowest,
    )
}

@Composable
private fun DeleteDayDialog(
    label: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear $label?") },
        text = { Text("Delete all transactions from $label? This cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete all", color = PaisaColors.Tertiary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = PaisaColors.SurfaceContainerLowest,
    )
}
