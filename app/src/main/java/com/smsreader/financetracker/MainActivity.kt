package com.smsreader.financetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smsreader.financetracker.ui.ViewModelFactory
import com.smsreader.financetracker.ui.navigation.Screen
import com.smsreader.financetracker.ui.screens.dashboard.DashboardScreen
import com.smsreader.financetracker.ui.screens.settings.SettingsScreen
import com.smsreader.financetracker.ui.screens.wallets.WalletsScreen
import com.smsreader.financetracker.ui.theme.SMSFinanceTrackerTheme

class MainActivity : ComponentActivity() {
    
    private var hasReadSmsPermission = false
    private var hasReceiveSmsPermission = false
    private var hasNotificationPermission = false
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasReadSmsPermission = permissions[Manifest.permission.READ_SMS] ?: false
        hasReceiveSmsPermission = permissions[Manifest.permission.RECEIVE_SMS] ?: false
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else {
            true
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkAndRequestPermissions()
        
        setContent {
            SMSFinanceTrackerTheme {
                MainScreen(viewModelFactory = ViewModelFactory(this))
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        hasReadSmsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        hasReceiveSmsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        val permissionsToRequest = mutableListOf<String>()
        
        if (!hasReadSmsPermission) {
            permissionsToRequest.add(Manifest.permission.READ_SMS)
        }
        
        if (!hasReceiveSmsPermission) {
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasNotificationPermission) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Dashboard.route } == true,
                    onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallets") },
                    label = { Text("Wallets") },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Wallets.route } == true,
                    onClick = {
                        navController.navigate(Screen.Wallets.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true,
                    onClick = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModelFactory = viewModelFactory)
            }
            composable(Screen.Wallets.route) {
                WalletsScreen(viewModelFactory = viewModelFactory)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModelFactory = viewModelFactory)
            }
        }
    }
}

