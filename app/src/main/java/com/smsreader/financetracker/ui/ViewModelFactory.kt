package com.smsreader.financetracker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smsreader.financetracker.data.local.database.AppDatabase
import com.smsreader.financetracker.data.repository.TransactionRepository
import com.smsreader.financetracker.data.repository.WalletRepository
import com.smsreader.financetracker.ui.screens.dashboard.DashboardViewModel
import com.smsreader.financetracker.ui.screens.settings.SettingsViewModel
import com.smsreader.financetracker.ui.screens.wallets.WalletsViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    private val database = AppDatabase.getInstance(context)
    private val transactionRepository = TransactionRepository(database.transactionDao())
    private val walletRepository = WalletRepository(database.walletDao())
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(transactionRepository, walletRepository) as T
            }
            modelClass.isAssignableFrom(WalletsViewModel::class.java) -> {
                WalletsViewModel(walletRepository, transactionRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(transactionRepository, walletRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

