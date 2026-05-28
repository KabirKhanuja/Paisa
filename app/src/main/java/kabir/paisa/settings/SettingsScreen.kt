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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kabir.paisa.ui.theme.PaisaColors

@Composable
fun SettingsScreen(
    onSignedOut: () -> Unit,
    onTab: (NavTab) -> Unit,
) {
    val ctx = LocalContext.current
    val email by AuthRepository.userEmail.collectAsStateWithLifecycle()

    var notificationsOn by remember { mutableStateOf(isListenerEnabled(ctx)) }
    var darkMode by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PaisaColors.Background,
        bottomBar = { PaisaBottomNav(NavTab.Settings, onTab) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // Profile header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaisaColors.Primary)
                    .padding(top = 48.dp, bottom = 40.dp, start = 20.dp, end = 20.dp),
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
                        (email?.firstOrNull()?.uppercase() ?: "P"),
                        color = PaisaColors.Primary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(Modifier.size(12.dp))
                Text("Paisa", color = PaisaColors.OnPrimary, style = MaterialTheme.typography.titleLarge)
                Text(email ?: "Not signed in", color = PaisaColors.OnPrimaryContainer, style = MaterialTheme.typography.labelLarge)
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Section("BANK") {
                    SettingRow(Icons.Filled.AccountBalance, "Connected bank", "Tap to configure")
                    SettingRow(Icons.Filled.SyncAlt, "Change bank", "Update connection")
                    SettingRow(Icons.Filled.AddCard, "Set starting balance", "Initial wallet state")
                }
                Section("PERMISSIONS") {
                    SettingToggleRow(
                        Icons.Filled.Notifications,
                        "Notifications",
                        if (notificationsOn) "Transaction listener: on" else "Tap to grant listener access",
                        checked = notificationsOn,
                        onChange = {
                            // Open system settings; the listener flag is read from the system.
                            ctx.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                            notificationsOn = isListenerEnabled(ctx)
                        }
                    )
                }
                Section("PREFERENCES") {
                    SettingRow(Icons.Filled.Category, "Manage categories", "Edit transaction tags")
                    SettingRow(Icons.Filled.Payments, "Currency (₹)", "Indian Rupee")
                    SettingRow(Icons.Filled.FormatSize, "Text size", "Adjust for better readability")
                    SettingToggleRow(
                        Icons.Filled.DarkMode,
                        "Dark mode",
                        "Theme settings",
                        checked = darkMode,
                        onChange = { darkMode = it }
                    )
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    OutlinedButton(
                        onClick = {
                            AuthRepository.signOut()
                            onSignedOut()
                        },
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Log out", color = PaisaColors.Tertiary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, color = PaisaColors.Outline, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(14.dp))
                .background(PaisaColors.SurfaceContainerLowest, RoundedCornerShape(14.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* hookup as needed */ }
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
