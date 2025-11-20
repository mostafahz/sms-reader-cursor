package com.smsreader.financetracker.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.smsreader.financetracker.ui.theme.*

object CategoryUtils {
    fun getCategoryColor(category: String): Color {
        return when (category) {
            "Food & Dining" -> FoodColor
            "Transportation" -> TransportColor
            "Health & Wellness" -> HealthColor
            "Shopping" -> ShoppingColor
            "Groceries" -> GroceriesColor
            "Utilities & Bills" -> UtilitiesColor
            "Entertainment" -> EntertainmentColor
            "Education" -> EducationColor
            "Travel" -> TravelColor
            else -> MiscColor
        }
    }

    fun getCategoryIcon(category: String): ImageVector {
        return when (category) {
            "Food & Dining" -> Icons.Default.Restaurant
            "Transportation" -> Icons.Default.DirectionsCar
            "Health & Wellness" -> Icons.Default.LocalHospital
            "Shopping" -> Icons.Default.ShoppingBag
            "Groceries" -> Icons.Default.ShoppingCart
            "Utilities & Bills" -> Icons.Default.Receipt
            "Entertainment" -> Icons.Default.Movie
            "Education" -> Icons.Default.School
            "Travel" -> Icons.Default.Flight
            else -> Icons.Default.Category
        }
    }

    fun getCategoryEmoji(category: String): String {
        return when (category) {
            "Food & Dining" -> "🍔"
            "Transportation" -> "🚗"
            "Health & Wellness" -> "💊"
            "Shopping" -> "🛍️"
            "Groceries" -> "🛒"
            "Utilities & Bills" -> "💡"
            "Entertainment" -> "🎬"
            "Education" -> "📚"
            "Travel" -> "✈️"
            else -> "📦"
        }
    }
}

