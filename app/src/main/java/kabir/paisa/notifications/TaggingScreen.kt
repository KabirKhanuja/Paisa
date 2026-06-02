package kabir.paisa.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import kabir.paisa.amount.CategoryPickerSheet
import kabir.paisa.common.formatRupees
import kabir.paisa.common.relativeDayLabel
import kabir.paisa.data.PaisaRepository
import kabir.paisa.data.Transaction
import kabir.paisa.ui.theme.PaisaColors
import kotlinx.coroutines.launch

@Composable
fun TaggingScreen(onBack: () -> Unit) {
    val txs by PaisaRepository.transactions.collectAsStateWithLifecycle()
    val untagged = txs.filter { it.category.isBlank() && it.type == PaisaRepository.TYPE_DEBIT }

    var pickingFor by remember { mutableStateOf<Transaction?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = PaisaColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PaisaColors.Primary)
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PaisaColors.OnPrimary)
                    }
                    Spacer(Modifier.size(4.dp))
                    Text("Tag transactions", color = PaisaColors.OnPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                        .background(PaisaColors.SecondaryFixed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, null, tint = PaisaColors.Primary)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text("${untagged.size} payments untagged", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Takes 30 seconds to clear", color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("UNTAGGED", color = PaisaColors.Outline, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(8.dp))
            }
            items(untagged) { tx ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                        .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                        .clickable { pickingFor = tx }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PaisaColors.SurfaceContainer, RoundedCornerShape(999.dp)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.AutoMirrored.Filled.HelpOutline, null, tint = PaisaColors.Outline) }
                        Spacer(Modifier.size(12.dp))
                        Column {
                            val label = if (tx.note.isNotBlank()) tx.note else "Transaction"
                            Text("${formatRupees(tx.amount, withDecimals = false)} · $label", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(relativeDayLabel(tx.date), color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text("Tag →", color = PaisaColors.Primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            if (untagged.isEmpty()) {
                item {
                    Text(
                        "All caught up. Nothing to tag.",
                        modifier = Modifier.padding(20.dp),
                        color = PaisaColors.Outline
                    )
                }
            }
            item { Spacer(Modifier.size(24.dp)) }
        }
    }

    pickingFor?.let { tx ->
        CategoryPickerSheet(
            onPick = { cat ->
                scope.launch { PaisaRepository.tagTransaction(tx.id, cat.name) }
                pickingFor = null
            },
            onDismiss = { pickingFor = null }
        )
    }
}
