package com.toting.ledger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps stable string keys (stored on categories/accounts) to Material icons.
 * The ordered map also backs the icon picker in category/account editing.
 */
object CategoryIcons {

    val map: Map<String, ImageVector> = linkedMapOf(
        "restaurant" to Icons.Filled.Restaurant,
        "local_cafe" to Icons.Filled.LocalCafe,
        "directions_bus" to Icons.Filled.DirectionsBus,
        "directions_car" to Icons.Filled.DirectionsCar,
        "local_gas_station" to Icons.Filled.LocalGasStation,
        "shopping_bag" to Icons.Filled.ShoppingBag,
        "shopping_cart" to Icons.Filled.ShoppingCart,
        "checkroom" to Icons.Filled.Checkroom,
        "home" to Icons.Filled.Home,
        "bolt" to Icons.Filled.Bolt,
        "phone_android" to Icons.Filled.PhoneAndroid,
        "sports_esports" to Icons.Filled.SportsEsports,
        "movie" to Icons.Filled.Movie,
        "fitness_center" to Icons.Filled.FitnessCenter,
        "local_hospital" to Icons.Filled.LocalHospital,
        "medical_services" to Icons.Filled.MedicalServices,
        "school" to Icons.Filled.School,
        "menu_book" to Icons.Filled.MenuBook,
        "pets" to Icons.Filled.Pets,
        "flight" to Icons.Filled.Flight,
        "hotel" to Icons.Filled.Hotel,
        "child_care" to Icons.Filled.ChildCare,
        "favorite" to Icons.Filled.Favorite,
        "spa" to Icons.Filled.Spa,
        "redeem" to Icons.Filled.Redeem,
        "payments" to Icons.Filled.Payments,
        "savings" to Icons.Filled.Savings,
        "trending_up" to Icons.Filled.TrendingUp,
        "attach_money" to Icons.Filled.AttachMoney,
        "work" to Icons.Filled.Work,
        "wallet" to Icons.Filled.AccountBalanceWallet,
        "account_balance" to Icons.Filled.AccountBalance,
        "credit_card" to Icons.Filled.CreditCard,
        "more_horiz" to Icons.Filled.MoreHoriz,
        "category" to Icons.Filled.Category,
    )

    val keys: List<String> = map.keys.toList()

    fun iconFor(key: String?): ImageVector = map[key] ?: Icons.Filled.Category
}
