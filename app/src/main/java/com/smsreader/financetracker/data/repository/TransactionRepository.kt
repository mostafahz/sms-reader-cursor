package com.smsreader.financetracker.data.repository

import com.smsreader.financetracker.data.local.dao.TransactionDao
import com.smsreader.financetracker.data.local.entities.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startTime, endTime)

    fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByWallet(walletId)

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategory(category)

    suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun insertTransactions(transactions: List<Transaction>) =
        transactionDao.insertTransactions(transactions)

    suspend fun clearAll() =
        transactionDao.clearAll()

    fun getTotalSpent(startTime: Long): Flow<Double?> =
        transactionDao.getTotalSpent(startTime)

    fun getTransactionCount(startTime: Long): Flow<Int> =
        transactionDao.getTransactionCount(startTime)

    fun getSpendingByCategory(startTime: Long) =
        transactionDao.getSpendingByCategory(startTime)

    fun getSpendingByWallet(startTime: Long) =
        transactionDao.getSpendingByWallet(startTime)
}

