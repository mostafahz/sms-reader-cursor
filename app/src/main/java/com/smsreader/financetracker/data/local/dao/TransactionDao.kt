package com.smsreader.financetracker.data.local.dao

import androidx.room.*
import com.smsreader.financetracker.data.local.entities.Transaction
import com.smsreader.financetracker.data.local.entities.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE wallet_id = :walletId ORDER BY timestamp DESC")
    fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Query("SELECT SUM(amount) FROM transactions WHERE transaction_type = 'DEBIT' AND timestamp >= :startTime")
    fun getTotalSpent(startTime: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp >= :startTime")
    fun getTransactionCount(startTime: Long): Flow<Int>

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE transaction_type = 'DEBIT' AND timestamp >= :startTime 
        GROUP BY category 
        ORDER BY total DESC
    """)
    fun getSpendingByCategory(startTime: Long): Flow<List<CategorySpending>>

    @Query("""
        SELECT wallet_id, SUM(amount) as total 
        FROM transactions 
        WHERE transaction_type = 'DEBIT' AND timestamp >= :startTime 
        GROUP BY wallet_id 
        ORDER BY total DESC
    """)
    fun getSpendingByWallet(startTime: Long): Flow<List<WalletSpending>>
}

data class CategorySpending(
    val category: String,
    val total: Double
)

data class WalletSpending(
    @ColumnInfo(name = "wallet_id")
    val walletId: String,
    val total: Double
)

