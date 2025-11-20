package com.smsreader.financetracker.data.repository

import com.smsreader.financetracker.data.local.dao.WalletDao
import com.smsreader.financetracker.data.local.entities.Wallet
import kotlinx.coroutines.flow.Flow

class WalletRepository(private val walletDao: WalletDao) {

    fun getAllWallets(): Flow<List<Wallet>> =
        walletDao.getAllWallets()

    suspend fun getWalletById(walletId: String): Wallet? =
        walletDao.getWalletById(walletId)

    suspend fun insertWallet(wallet: Wallet): Long =
        walletDao.insertWallet(wallet)

    suspend fun updateWallet(wallet: Wallet) =
        walletDao.updateWallet(wallet)

    suspend fun updateCustomName(walletId: String, customName: String) =
        walletDao.updateCustomName(walletId, customName)

    suspend fun updateLastUsed(walletId: String, lastUsed: Long) =
        walletDao.updateLastUsed(walletId, lastUsed)

    suspend fun clearAll() =
        walletDao.clearAll()

    fun getWalletCount(): Flow<Int> =
        walletDao.getWalletCount()
}

