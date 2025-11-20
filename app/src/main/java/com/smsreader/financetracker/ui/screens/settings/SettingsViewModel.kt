package com.smsreader.financetracker.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsreader.financetracker.data.repository.TransactionRepository
import com.smsreader.financetracker.data.repository.WalletRepository
import com.smsreader.financetracker.util.CsvExporter
import com.smsreader.financetracker.util.SmsScanner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<SmsScanner.ScanResult?>(null)
    val scanResult: StateFlow<SmsScanner.ScanResult?> = _scanResult.asStateFlow()

    private val _senderIds = MutableStateFlow<List<String>>(emptyList())
    val senderIds: StateFlow<List<String>> = _senderIds.asStateFlow()

    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportStatus: StateFlow<ExportStatus> = _exportStatus.asStateFlow()

    fun loadSenderIds(context: Context) {
        viewModelScope.launch {
            val ids = SmsScanner.getAllSenderIds(context)
            _senderIds.value = ids
        }
    }

    fun scanMessages(context: Context, senderId: String?, messageCount: Int) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = null

            try {
                val result = SmsScanner.scanSmsMessages(context, senderId, messageCount)
                _scanResult.value = result
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            transactionRepository.clearAll()
            walletRepository.clearAll()
            _scanResult.value = null
        }
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Exporting

            transactionRepository.getAllTransactions().firstOrNull()?.let { transactions ->
                val result = CsvExporter.exportTransactionsToCsv(context, transactions)

                if (result.isSuccess) {
                    val file = result.getOrNull()
                    if (file != null) {
                        _exportStatus.value = ExportStatus.Success(file.absolutePath)
                        // Trigger share
                        CsvExporter.shareFile(context, file)
                    }
                } else {
                    _exportStatus.value = ExportStatus.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } ?: run {
                _exportStatus.value = ExportStatus.Error("No transactions to export")
            }
        }
    }

    fun resetExportStatus() {
        _exportStatus.value = ExportStatus.Idle
    }

    sealed class ExportStatus {
        object Idle : ExportStatus()
        object Exporting : ExportStatus()
        data class Success(val filePath: String) : ExportStatus()
        data class Error(val message: String) : ExportStatus()
    }
}

