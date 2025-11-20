package com.smsreader.financetracker.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.smsreader.financetracker.data.local.database.AppDatabase
import com.smsreader.financetracker.data.local.entities.Transaction
import com.smsreader.financetracker.data.local.entities.TransactionType
import com.smsreader.financetracker.data.local.entities.Wallet
import com.smsreader.financetracker.domain.SmsClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsScanner {
    
    data class ScanResult(
        val scannedCount: Int,
        val foundTransactions: Int
    )
    
    suspend fun scanSmsMessages(
        context: Context,
        senderId: String? = null,
        limit: Int = 100
    ): ScanResult = withContext(Dispatchers.IO) {
        try {
            val messages = readSmsMessages(context, senderId, limit)
            val database = AppDatabase.getInstance(context)
            var foundCount = 0
            
            for (message in messages) {
                try {
                    // Check if it's a banking SMS
                    if (!SmsClassifier.isBankingSms(message.sender, message.body)) {
                        continue
                    }
                    
                    // Classify the SMS
                    val result = SmsClassifier.classifySms(message.body, message.sender) ?: continue
                    
                    // Only process debit transactions
                    if (result.transactionType != TransactionType.DEBIT) {
                        continue
                    }
                    
                    // Check if transaction already exists (prevent duplicates)
                    val exists = database.transactionDao().transactionExists(
                        message.sender, 
                        message.body, 
                        message.timestamp
                    )
                    if (exists) {
                        continue
                    }
                    
                    // Create transaction
                    val transaction = Transaction(
                        amount = result.amount,
                        category = result.category,
                        merchant = result.merchant,
                        walletId = result.walletId,
                        transactionType = result.transactionType,
                        timestamp = message.timestamp,
                        smsBody = message.body,
                        senderId = message.sender
                    )
                    
                    // Save transaction
                    database.transactionDao().insertTransaction(transaction)
                    foundCount++
                    
                    // Create or update wallet
                    val existingWallet = database.walletDao().getWalletById(result.walletId)
                    if (existingWallet == null) {
                        val wallet = Wallet(
                            walletId = result.walletId,
                            detectedName = result.detectedWalletName,
                            bankName = result.bankName,
                            cardType = result.cardType,
                            lastUsed = message.timestamp
                        )
                        database.walletDao().insertWallet(wallet)
                    } else {
                        // Update last used if this transaction is newer
                        if (message.timestamp > existingWallet.lastUsed) {
                            database.walletDao().updateLastUsed(result.walletId, message.timestamp)
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e("SmsScanner", "Error processing message", e)
                }
            }
            
            ScanResult(messages.size, foundCount)
        } catch (e: Exception) {
            Log.e("SmsScanner", "Error scanning SMS", e)
            ScanResult(0, 0)
        }
    }
    
    private fun readSmsMessages(
        context: Context,
        senderId: String?,
        limit: Int
    ): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        
        try {
            val uri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("address", "body", "date")
            
            val selection = if (senderId != null) "address = ?" else null
            val selectionArgs = if (senderId != null) arrayOf(senderId) else null
            
            val cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "date DESC"
            )
            
            cursor?.use {
                val addressIndex = it.getColumnIndex("address")
                val bodyIndex = it.getColumnIndex("body")
                val dateIndex = it.getColumnIndex("date")
                
                var count = 0
                while (it.moveToNext() && count < limit) {
                    val address = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: continue
                    val date = it.getLong(dateIndex)
                    
                    messages.add(SmsMessage(address, body, date))
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e("SmsScanner", "Error reading SMS", e)
        }
        
        return messages
    }
    
    fun getAllSenderIds(context: Context): List<String> {
        val senders = mutableSetOf<String>()
        
        try {
            val uri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("address")
            
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "date DESC"
            )
            
            cursor?.use {
                val addressIndex = it.getColumnIndex("address")
                
                while (it.moveToNext()) {
                    val address = it.getString(addressIndex)
                    if (address != null && address.isNotBlank()) {
                        senders.add(address)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsScanner", "Error reading sender IDs", e)
        }
        
        return senders.toList().sorted()
    }
    
    private data class SmsMessage(
        val sender: String,
        val body: String,
        val timestamp: Long
    )
}

