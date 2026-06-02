package kabir.paisa.settings

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kabir.paisa.common.ui.NavTab
import kabir.paisa.common.ui.PaisaBottomNav
import kabir.paisa.data.AuthRepository
import kabir.paisa.data.PaisaRepository
import kabir.paisa.ui.theme.PaisaColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onSignedOut: () -> Unit,
    onDataCleared: () -> Unit,
    onTab: (NavTab) -> Unit,
) {
    val ctx = LocalContext.current
    val email by AuthRepository.userEmail.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var notificationsOn by remember { mutableStateOf(isListenerEnabled(ctx)) }
    var darkMode by remember { mutableStateOf(false) }
    var showClearAll by remember { mutableStateOf(false) }
    var clearing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PaisaColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { PaisaBottomNav(NavTab.Settings, onTab) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PaisaColors.Primary)
                        .statusBarsPadding()
                        .padding(top = 32.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(PaisaColors.SurfaceContainerLowest, CircleShape)
                            .border(0.5.dp, PaisaColors.OutlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            email?.firstOrNull()?.uppercase() ?: "P",
                            color = PaisaColors.Primary,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Text("Paisa", color = PaisaColors.OnPrimary, style = MaterialTheme.typography.titleLarge)
                    Text(email ?: "Not signed in", color = PaisaColors.OnPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                Section(title = "BANK") {
                    SettingRow(Icons.Filled.AccountBalance, "Connected bank", "Tap to configure")
                    SettingDivider()
                    SettingRow(Icons.Filled.SyncAlt, "Change bank", "Update connection")
                    SettingDivider()
                    SettingRow(Icons.Filled.AddCard, "Set starting balance", "Initial wallet state")
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                Section(title = "PERMISSIONS") {
                    SettingToggleRow(
                        icon = Icons.Filled.Notifications,
                        title = "Notifications",
                        subtitle = if (notificationsOn) "Transaction listener: on" else "Tap to grant listener access",
                        checked = notificationsOn,
                        onChange = {
                            ctx.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                            notificationsOn = isListenerEnabled(ctx)
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                Section(title = "PREFERENCES") {
                    SettingRow(Icons.Filled.Category, "Manage categories", "Edit transaction tags")
                    SettingDivider()
                    SettingRow(Icons.Filled.Payments, "Currency (₹)", "Indian Rupee")
                    SettingDivider()
                    SettingRow(Icons.Filled.FormatSize, "Text size", "Adjust for better readability")
                    SettingDivider()
                    SettingToggleRow(
                        icon = Icons.Filled.DarkMode,
                        title = "Dark mode",
                        subtitle = "Theme settings",
                        checked = darkMode,
                        onChange = { darkMode = it }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                Section(title = "DANGER ZONE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !clearing) { showClearAll = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DeleteForever, null, tint = PaisaColors.Tertiary)
                            Spacer(Modifier.size(16.dp))
                            Column {
                                Text(
                                    if (clearing) "Clearing…" else "Clear all data",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PaisaColors.Tertiary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "Wipe transactions, snapshots and reports",
                                    color = PaisaColors.Outline,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PaisaColors.OutlineVariant)
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            AuthRepository.signOut()
                            onSignedOut()
                        },
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text("Log out", color = PaisaColors.Tertiary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showClearAll) {
        ClearAllDataDialog(
            onConfirm = {
                showClearAll = false
                clearing = true
                scope.launch {
                    runCatching { PaisaRepository.clearAllData() }
                        .onSuccess { onDataCleared() }
                    clearing = false
                }
            },
            onDismiss = { showClearAll = false },
        )
    }
}

@Composable
private fun ClearAllDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear all data?") },
        text = {
            Text("This will permanently delete all your transactions, snapshots and reports. This cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Clear everything", color = PaisaColors.Tertiary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = PaisaColors.SurfaceContainerLowest,
    )
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = title,
            color = PaisaColors.Outline,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(14.dp))
                .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(14.dp)),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(PaisaColors.OutlineVariant.copy(alpha = 0.3f))
    )
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: hook each row */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PaisaColors.Primary)
            Spacer(Modifier.size(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(subtitle, color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PaisaColors.OutlineVariant)
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, null, tint = PaisaColors.Primary)
            Spacer(Modifier.size(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(subtitle, color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PaisaColors.OnPrimary,
                checkedTrackColor = PaisaColors.Primary,
                uncheckedThumbColor = PaisaColors.OnPrimary,
                uncheckedTrackColor = PaisaColors.OutlineVariant,
            )
        )
    }
}

private fun isListenerEnabled(ctx: android.content.Context): Boolean {
    val flat = Settings.Secure.getString(ctx.contentResolver, "enabled_notification_listeners") ?: return false
    val component = ComponentName(ctx, "kabir.paisa.notifications.PaisaNotificationListener").flattenToString()
    return flat.contains(component)
}
