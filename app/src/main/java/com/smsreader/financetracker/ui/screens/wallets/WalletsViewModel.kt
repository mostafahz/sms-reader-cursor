package com.smsreader.financetracker.ui.screens.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsreader.financetracker.data.local.entities.Wallet
import com.smsreader.financetracker.data.repository.TransactionRepository
import com.smsreader.financetracker.data.repository.WalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WalletsViewModel(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val wallets: StateFlow<List<Wallet>> = walletRepository.getAllWallets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _walletSpending = MutableStateFlow<Map<String, Double>>(emptyMap())
    val walletSpending: StateFlow<Map<String, Double>> = _walletSpending.asStateFlow()

    private val _walletTransactionCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val walletTransactionCounts: StateFlow<Map<String, Int>> = _walletTransactionCounts.asStateFlow()

    init {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                val spending = transactions
                    .groupBy { it.walletId }
                    .mapValues { (_, txs) -> txs.sumOf { it.amount } }

                val counts = transactions
                    .groupBy { it.walletId }
                    .mapValues { (_, txs) -> txs.size }

                _walletSpending.value = spending
                _walletTransactionCounts.value = counts
            }
        }
    }

    fun renameWallet(walletId: String, customName: String) {
        viewModelScope.launch {
            walletRepository.updateCustomName(walletId, customName)
        }
    }

    fun getWalletDisplayName(wallet: Wallet): String {
        return wallet.customName ?: wallet.detectedName
    }
}

