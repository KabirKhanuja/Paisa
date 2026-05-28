package kabir.paisa.data.model

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val iconName: String, // material symbol name
)

object Categories {
    val Defaults = listOf(
        Category("food", "Food", "🍔", "restaurant"),
        Category("transport", "Transport", "🚌", "directions_bus"),
        Category("shopping", "Shopping", "🛒", "shopping_bag"),
        Category("petrol", "Petrol", "⛽", "local_gas_station"),
        Category("friend", "Friend", "👤", "person"),
        Category("health", "Health", "💊", "medication"),
        Category("bills", "Bills", "📱", "payments"),
        Category("outing", "Outing", "🎉", "celebration"),
        Category("salary", "Salary", "💰", "payments"),
        Category("other", "Other", "•••", "more_horiz"),
    )

    fun byId(id: String?): Category? = id?.let { Defaults.firstOrNull { c -> c.id == it } }
}
