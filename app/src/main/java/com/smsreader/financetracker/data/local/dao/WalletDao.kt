package com.smsreader.financetracker.data.local.dao

import androidx.room.*
import com.smsreader.financetracker.data.local.entities.Wallet
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY last_used DESC")
    fun getAllWallets(): Flow<List<Wallet>>

    @Query("SELECT * FROM wallets WHERE wallet_id = :walletId")
    suspend fun getWalletById(walletId: String): Wallet?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallet(wallet: Wallet): Long

    @Update
    suspend fun updateWallet(wallet: Wallet)

    @Query("UPDATE wallets SET custom_name = :customName, last_used = :lastUsed WHERE wallet_id = :walletId")
    suspend fun updateCustomName(walletId: String, customName: String, lastUsed: Long = System.currentTimeMillis())

    @Query("UPDATE wallets SET last_used = :lastUsed WHERE wallet_id = :walletId")
    suspend fun updateLastUsed(walletId: String, lastUsed: Long)

    @Query("DELETE FROM wallets")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM wallets")
    fun getWalletCount(): Flow<Int>
}

