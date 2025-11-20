package com.smsreader.financetracker.domain.classifier

import com.smsreader.financetracker.data.local.entities.TransactionType

object TransactionTypeDetector {
    private val debitKeywords = listOf(
        "debited", "spent", "paid", "withdrawn", "purchase", "deducted", 
        "charged", "debited from", "paid to", "debit", "payment",
        "transaction of", "payment of", "was done", "محاولة", "حجزت"
    )

    private val creditKeywords = listOf(
        "credited", "received", "deposited", "refund", "cashback", 
        "earned", "credited to", "credit", "received from", "added to"
    )

    fun detectType(smsBody: String): TransactionType {
        val lowerBody = smsBody.lowercase()

        // Check for debit keywords first (more common)
        for (keyword in debitKeywords) {
            if (lowerBody.contains(keyword)) {
                return TransactionType.DEBIT
            }
        }

        // Then check for credit keywords
        for (keyword in creditKeywords) {
            if (lowerBody.contains(keyword)) {
                return TransactionType.CREDIT
            }
        }

        // Default assumption for transaction SMS
        return TransactionType.DEBIT
    }
}

