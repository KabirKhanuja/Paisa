package kabir.paisa.common.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.ui.graphics.vector.ImageVector

fun iconForName(name: String): ImageVector = when (name) {
    "restaurant", "food" -> Icons.Filled.Restaurant
    "directions_bus", "transport" -> Icons.Filled.DirectionsBus
    "shopping_bag", "shopping_cart", "shopping" -> Icons.Filled.ShoppingBag
    "local_gas_station", "petrol", "fuel" -> Icons.Filled.LocalGasStation
    "person", "friend" -> Icons.Filled.Person
    "medication", "health" -> Icons.Filled.Medication
    "payments", "bills", "salary" -> Icons.Filled.Payments
    "celebration", "outing" -> Icons.Filled.Celebration
    "smart_toy" -> Icons.Filled.SmartToy
    "music_note" -> Icons.Filled.MusicNote
    "subscriptions" -> Icons.Filled.Subscriptions
    "wifi" -> Icons.Filled.Wifi
    "shield" -> Icons.Filled.Shield
    "directions_car" -> Icons.Filled.DirectionsCar
    "home" -> Icons.Filled.Home
    "coffee" -> Icons.Filled.Coffee
    "more_horiz", "other" -> Icons.Filled.MoreHoriz
    else -> Icons.AutoMirrored.Filled.HelpOutline
}
