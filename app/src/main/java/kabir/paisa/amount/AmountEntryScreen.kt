package kabir.paisa.amount

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kabir.paisa.common.ui.CategoryDef
import kabir.paisa.data.PaisaRepository
import kabir.paisa.ui.theme.PaisaColors
import kabir.paisa.ui.theme.PaisaTextStyles

@Composable
fun AmountEntryScreen(
    initialIsAdd: Boolean,
    onBack: () -> Unit,
    onConfirmed: () -> Unit,
) {
    var isAdd by remember { mutableStateOf(initialIsAdd) }
    var amount by remember { mutableStateOf("0") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryDef?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    fun append(ch: String) {
        amount = when {
            amount == "0" && ch != "." -> ch
            ch == "." && amount.contains(".") -> amount
            amount.contains(".") && amount.substringAfter(".").length >= 2 -> amount
            amount.length >= 9 -> amount
            else -> amount + ch
        }
    }
    fun backspace() { amount = if (amount.length <= 1) "0" else amount.dropLast(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaisaColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PaisaColors.Surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PaisaColors.Primary)
            }
            Spacer(Modifier.size(4.dp))
            Text(
                "New Transaction",
                color = PaisaColors.Primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(PaisaColors.OutlineVariant))

        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 24.dp)) {
            // Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaisaColors.SurfaceContainerLow, RoundedCornerShape(12.dp))
                    .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                listOf(false, true).forEach { add ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isAdd == add) PaisaColors.Primary else androidx.compose.ui.graphics.Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { isAdd = add }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (add) "Add" else "Subtract",
                            color = if (isAdd == add) PaisaColors.OnPrimary else PaisaColors.OnSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("AMOUNT", color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹",
                        style = PaisaTextStyles.AmountDisplay,
                        color = if (isAdd) PaisaColors.Primary else PaisaColors.Tertiary
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(amount, style = PaisaTextStyles.AmountDisplay, color = PaisaColors.OnSurface)
                }
            }

            BasicTextField(
                value = description,
                onValueChange = { description = it },
                textStyle = TextStyle(fontSize = 16.sp, color = PaisaColors.OnSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                    .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                decorationBox = { inner ->
                    if (description.isEmpty()) {
                        Text("What was this for?", color = PaisaColors.Outline.copy(alpha = 0.6f))
                    }
                    inner()
                }
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                    .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                    .clickable { showCategoryPicker = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Category, null, tint = PaisaColors.Outline)
                    Spacer(Modifier.size(12.dp))
                    Text(
                        selectedCategory?.name ?: "Select category",
                        color = if (selectedCategory != null) PaisaColors.OnSurface else PaisaColors.OnSurfaceVariant
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PaisaColors.Outline)
            }

            Spacer(Modifier.height(24.dp))

            val keys = listOf("1","2","3","4","5","6","7","8","9",".","0","⌫")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(keys.size, key = { it }) { idx ->
                    val k = keys[idx]
                    Box(
                        modifier = Modifier
                            .aspectRatio(1.6f)
                            .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(12.dp))
                            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(12.dp))
                            .clickable { if (k == "⌫") backspace() else append(k) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (k == "⌫") {
                            Icon(Icons.AutoMirrored.Filled.Backspace, null, tint = PaisaColors.Tertiary)
                        } else {
                            Text(k, style = MaterialTheme.typography.titleLarge, color = PaisaColors.OnSurface)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val parsed = amount.toDoubleOrNull() ?: 0.0
                    if (parsed > 0) {
                        PaisaRepository.addTransaction(
                            amount = parsed,
                            type = if (isAdd) PaisaRepository.TYPE_CREDIT else PaisaRepository.TYPE_DEBIT,
                            category = selectedCategory?.name ?: "",
                            note = description.ifBlank { if (isAdd) "Added" else "Spent" },
                            source = PaisaRepository.SOURCE_MANUAL,
                        )
                        onConfirmed()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaisaColors.Primary,
                    contentColor = PaisaColors.OnPrimary
                )
            ) {
                Text("Confirm Transaction", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showCategoryPicker) {
        CategoryPickerSheet(
            onPick = { selectedCategory = it; showCategoryPicker = false },
            onDismiss = { showCategoryPicker = false }
        )
    }
}
