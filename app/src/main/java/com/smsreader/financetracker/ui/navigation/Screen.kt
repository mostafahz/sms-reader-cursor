package com.smsreader.financetracker.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Wallets : Screen("wallets")
    object Settings : Screen("settings")
}

