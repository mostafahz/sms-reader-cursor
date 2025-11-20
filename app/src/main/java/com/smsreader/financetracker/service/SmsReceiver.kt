package com.smsreader.financetracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.smsreader.financetracker.data.local.database.AppDatabase
import com.smsreader.financetracker.data.local.entities.Transaction
import com.smsreader.financetracker.data.local.entities.TransactionType
import com.smsreader.financetracker.data.local.entities.Wallet
import com.smsreader.financetracker.domain.SmsClassifier
import com.smsreader.financetracker.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle = intent.extras ?: return
            val pdus = bundle.get("pdus") as? Array<*> ?: return
            
            for (pdu in pdus) {
                val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                val senderId = sms.originatingAddress ?: continue
                val smsBody = sms.messageBody ?: continue
                val timestamp = sms.timestampMillis
                
                Log.d("SmsReceiver", "Received SMS from $senderId")
                
                // Process SMS in background
                scope.launch {
                    processSms(context, senderId, smsBody, timestamp)
                }
            }
        }
    }
    
    private suspend fun processSms(context: Context, senderId: String, smsBody: String, timestamp: Long) {
        try {
            // Check if it's a banking SMS
            if (!SmsClassifier.isBankingSms(senderId, smsBody)) {
                Log.d("SmsReceiver", "Not a banking SMS, skipping")
                return
            }
            
            // Classify the SMS
            val result = SmsClassifier.classifySms(smsBody, senderId) ?: run {
                Log.d("SmsReceiver", "Could not classify SMS")
                return
            }
            
            // Only process debit transactions for spending tracking
            if (result.transactionType != TransactionType.DEBIT) {
                Log.d("SmsReceiver", "Credit transaction, skipping")
                return
            }
            
            val database = AppDatabase.getInstance(context)
            
            // Create transaction
            val transaction = Transaction(
                amount = result.amount,
                category = result.category,
                merchant = result.merchant,
                walletId = result.walletId,
                transactionType = result.transactionType,
                timestamp = timestamp,
                smsBody = smsBody,
                senderId = senderId
            )
            
            // Save transaction to database
            val transactionId = database.transactionDao().insertTransaction(transaction)
            Log.d("SmsReceiver", "Transaction saved: ID=$transactionId, Amount=${result.amount}, Category=${result.category}")
            
            // Create or update wallet
            val existingWallet = database.walletDao().getWalletById(result.walletId)
            if (existingWallet == null) {
                val wallet = Wallet(
                    walletId = result.walletId,
                    detectedName = result.detectedWalletName,
                    bankName = result.bankName,
                    cardType = result.cardType,
                    lastUsed = timestamp
                )
                database.walletDao().insertWallet(wallet)
                Log.d("SmsReceiver", "New wallet created: ${result.walletId}")
            } else {
                // Update last used time
                database.walletDao().updateLastUsed(result.walletId, timestamp)
            }
            
            // Show notification
            NotificationHelper.showTransactionNotification(
                context = context,
                amount = result.amount,
                merchant = result.merchant,
                category = result.category
            )
            
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error processing SMS", e)
        }
    }
}

