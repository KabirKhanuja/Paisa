package kabir.paisa.common.ui

/**
 * UI-side metadata for category strings stored in Firestore.
 * The canonical [name] is the exact string written to `transaction.category`.
 */
data class CategoryDef(
    val name: String,
    val emoji: String,
    val iconName: String,
)

object CategoryDefs {
    val All = listOf(
        CategoryDef("Food", "🍔", "restaurant"),
        CategoryDef("Transport", "🚌", "directions_bus"),
        CategoryDef("Shopping", "🛒", "shopping_bag"),
        CategoryDef("Petrol", "⛽", "local_gas_station"),
        CategoryDef("Friends", "👤", "person"),
        CategoryDef("Health", "💊", "medication"),
        CategoryDef("Bills", "📱", "payments"),
        CategoryDef("Outing", "🎉", "celebration"),
        CategoryDef("Income", "💰", "payments"),
        CategoryDef("Other", "•••", "more_horiz"),
    )

    fun byName(name: String?): CategoryDef? =
        if (name.isNullOrBlank()) null else All.firstOrNull { it.name.equals(name, ignoreCase = true) }
}
