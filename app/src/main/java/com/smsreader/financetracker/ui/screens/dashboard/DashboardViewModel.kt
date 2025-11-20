package com.smsreader.financetracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsreader.financetracker.data.local.dao.CategorySpending
import com.smsreader.financetracker.data.local.dao.WalletSpending
import com.smsreader.financetracker.data.local.entities.Transaction
import com.smsreader.financetracker.data.repository.TransactionRepository
import com.smsreader.financetracker.data.repository.WalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.THIS_MONTH)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val startTime: StateFlow<Long> = _selectedTimeRange.map { range ->
        calculateStartTime(range)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), calculateStartTime(TimeRange.THIS_MONTH))

    val transactions: StateFlow<List<Transaction>> = startTime.flatMapLatest { start ->
        transactionRepository.getAllTransactions()
            .map { list -> list.filter { it.timestamp >= start } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpent: StateFlow<Double> = startTime.flatMapLatest { start ->
        transactionRepository.getTotalSpent(start)
    }.map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val transactionCount: StateFlow<Int> = startTime.flatMapLatest { start ->
        transactionRepository.getTransactionCount(start)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val categorySpending: StateFlow<List<CategorySpending>> = startTime.flatMapLatest { start ->
        transactionRepository.getSpendingByCategory(start)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletSpending: StateFlow<List<WalletSpending>> = startTime.flatMapLatest { start ->
        transactionRepository.getSpendingByWallet(start)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topCategory: StateFlow<String?> = categorySpending.map { spending ->
        spending.maxByOrNull { it.total }?.category
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    private fun calculateStartTime(range: TimeRange): Long {
        val calendar = Calendar.getInstance()
        return when (range) {
            TimeRange.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            TimeRange.LAST_3_MONTHS -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.timeInMillis
            }
            TimeRange.ALL_TIME -> 0L
        }
    }

    enum class TimeRange {
        THIS_MONTH, LAST_3_MONTHS, ALL_TIME
    }
}

