package kabir.paisa.budget

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kabir.paisa.common.formatRupees
import kabir.paisa.common.ui.iconForName
import kabir.paisa.data.Budget
import kabir.paisa.data.FixedExpense
import kabir.paisa.data.PaisaRepository
import kabir.paisa.ui.theme.PaisaColors
import kabir.paisa.ui.theme.PaisaTextStyles
import kotlinx.coroutines.launch

@Composable
fun BudgetSetupScreen(onDone: () -> Unit) {
    val budget by PaisaRepository.budget.collectAsStateWithLifecycle()

    var salary by remember { mutableStateOf(budget.salary.toLong().toString()) }
    var cap by remember { mutableStateOf(budget.spendingCap.toLong().toString()) }
    val expenses = remember { mutableStateListOf<FixedExpense>().apply { addAll(budget.fixedExpenses) } }
    var newExpenseName by remember { mutableStateOf("") }
    var newExpenseAmount by remember { mutableStateOf("") }
    var showAddRow by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val totalFixed = expenses.sumOf { it.amount }
    val salaryVal = salary.toDoubleOrNull() ?: 0.0
    val capVal = cap.toDoubleOrNull() ?: 0.0
    val flexRemaining = (salaryVal - totalFixed).coerceAtLeast(0.0)
    val investments = (salaryVal - totalFixed - capVal).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaisaColors.Background)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PaisaColors.Primary)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountBalanceWallet, null, tint = PaisaColors.OnPrimary)
                Spacer(Modifier.size(8.dp))
                Text("Budget Setup", style = MaterialTheme.typography.titleLarge, color = PaisaColors.OnPrimary, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onDone) {
                Icon(Icons.Filled.MoreVert, null, tint = PaisaColors.OnPrimary)
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Monthly Salary", color = PaisaColors.Outline, style = MaterialTheme.typography.labelLarge)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(14.dp))
                        .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(14.dp))
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("₹", style = PaisaTextStyles.AmountDisplay, color = PaisaColors.Primary)
                        Spacer(Modifier.size(4.dp))
                        BasicTextField(
                            value = salary,
                            onValueChange = { salary = it.filter { ch -> ch.isDigit() } },
                            textStyle = PaisaTextStyles.AmountDisplay.copy(color = PaisaColors.OnSurface),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Enter your net take-home salary", color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fixed Expenses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("${expenses.size} Items", color = PaisaColors.Outline, style = MaterialTheme.typography.labelLarge)
                }
                expenses.forEach { e ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(14.dp))
                            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(PaisaColors.SecondaryFixed, RoundedCornerShape(999.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(iconForName(e.icon), null, tint = PaisaColors.OnSecondaryFixedVariant)
                            }
                            Spacer(Modifier.size(12.dp))
                            Text(e.name, color = PaisaColors.OnSurface)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatRupees(e.amount, withDecimals = false), color = PaisaColors.Secondary)
                            Spacer(Modifier.size(8.dp))
                            Text("✕", color = PaisaColors.Outline, modifier = Modifier.clickable { expenses.remove(e) })
                        }
                    }
                }
                if (showAddRow) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(14.dp))
                            .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BasicTextField(
                            value = newExpenseName,
                            onValueChange = { newExpenseName = it },
                            textStyle = TextStyle(fontSize = 16.sp, color = PaisaColors.OnSurface),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (newExpenseName.isEmpty()) Text("Name (e.g. Internet)", color = PaisaColors.Outline.copy(alpha = 0.6f))
                                inner()
                            }
                        )
                        BasicTextField(
                            value = newExpenseAmount,
                            onValueChange = { newExpenseAmount = it.filter { ch -> ch.isDigit() } },
                            textStyle = TextStyle(fontSize = 16.sp, color = PaisaColors.OnSurface),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (newExpenseAmount.isEmpty()) Text("Amount (₹)", color = PaisaColors.Outline.copy(alpha = 0.6f))
                                inner()
                            }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Save",
                                color = PaisaColors.Primary,
                                modifier = Modifier
                                    .clickable {
                                        val amt = newExpenseAmount.toDoubleOrNull() ?: 0.0
                                        if (newExpenseName.isNotBlank() && amt > 0) {
                                            expenses.add(FixedExpense(newExpenseName.trim(), amt, ""))
                                            newExpenseName = ""; newExpenseAmount = ""; showAddRow = false
                                        }
                                    }
                                    .padding(8.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Cancel",
                                color = PaisaColors.Outline,
                                modifier = Modifier
                                    .clickable { showAddRow = false; newExpenseName = ""; newExpenseAmount = "" }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(14.dp))
                        .clickable { showAddRow = true },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AddCircle, null, tint = PaisaColors.Primary)
                    Spacer(Modifier.size(6.dp))
                    Text("Add expense", color = PaisaColors.Primary, style = MaterialTheme.typography.labelLarge)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Spending Cap (Optional)", color = PaisaColors.Outline, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(14.dp))
                        .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.TrackChanges, null, tint = PaisaColors.Outline)
                    Spacer(Modifier.size(12.dp))
                    BasicTextField(
                        value = cap,
                        onValueChange = { cap = it.filter { ch -> ch.isDigit() } },
                        textStyle = TextStyle(fontSize = 18.sp, color = PaisaColors.OnSurface),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (cap.isEmpty()) Text("Limit your total spending", color = PaisaColors.Outline.copy(alpha = 0.6f))
                            inner()
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaisaColors.Primary, RoundedCornerShape(14.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("REMAINING FOR LEISURE", color = PaisaColors.OnPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    formatRupees(flexRemaining, withDecimals = false),
                    color = PaisaColors.OnPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Flex Budget Remaining", color = PaisaColors.OnPrimary, style = MaterialTheme.typography.labelSmall)
                if (investments > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("${formatRupees(investments, withDecimals = false)} marked as investment", color = PaisaColors.OnPrimaryContainer, style = MaterialTheme.typography.labelSmall)
                }
            }

            Button(
                onClick = {
                    if (saving) return@Button
                    saving = true
                    scope.launch {
                        runCatching {
                            PaisaRepository.updateBudget(
                                Budget(
                                    salary = salaryVal,
                                    spendingCap = capVal,
                                    investmentTarget = investments,
                                    flexBudget = capVal,
                                    fixedExpenses = expenses.toList(),
                                )
                            )
                        }
                        saving = false
                        onDone()
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaisaColors.Primary,
                    contentColor = PaisaColors.OnPrimary
                )
            ) {
                Text(
                    if (saving) "Saving…" else "Confirm Budget Plan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.size(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
