package com.smsreader.financetracker.domain

import com.smsreader.financetracker.domain.classifier.*
import com.smsreader.financetracker.domain.models.ClassificationResult

object SmsClassifier {
    
    fun classifySms(smsBody: String, senderId: String): ClassificationResult? {
        // Step 1: Extract amount
        val amount = AmountParser.extractAmount(smsBody) ?: return null
        
        // Step 2: Detect transaction type
        val transactionType = TransactionTypeDetector.detectType(smsBody)
        
        // Step 3: Extract merchant
        val merchant = MerchantExtractor.extractMerchant(smsBody)
        
        // Step 4: Classify category
        val category = CategoryClassifier.classify(merchant, smsBody)
        
        // Step 5: Detect wallet
        val walletId = WalletDetector.detectWallet(smsBody, senderId) ?: "UNKNOWN"
        
        // Step 6: Detect card type
        val cardType = WalletDetector.detectCardType(walletId, smsBody, senderId)
        
        // Step 7: Generate wallet name
        val detectedWalletName = WalletDetector.generateDetectedName(walletId, senderId)
        
        // Step 8: Extract bank name
        val bankName = WalletDetector.extractBankName(senderId)
        
        return ClassificationResult(
            amount = amount,
            merchant = merchant,
            category = category,
            walletId = walletId,
            cardType = cardType,
            transactionType = transactionType,
            detectedWalletName = detectedWalletName,
            bankName = bankName
        )
    }
    
    fun isBankingSms(senderId: String, smsBody: String): Boolean {
        // Check if sender ID looks like a bank
        val bankKeywords = listOf(
            "bank", "card", "atm", "paytm", "phonepe", "gpay", "upi",
            "hdfc", "icici", "sbi", "axis", "kotak"
        )
        
        val lowerSender = senderId.lowercase()
        val lowerBody = smsBody.lowercase()
        
        // Check sender ID
        val hasBankSender = bankKeywords.any { lowerSender.contains(it) }
        
        // Check for transaction keywords
        val hasTransactionKeywords = lowerBody.contains("debited") || 
                                     lowerBody.contains("credited") ||
                                     lowerBody.contains("spent") ||
                                     lowerBody.contains("paid") ||
                                     lowerBody.contains("withdrawn")
        
        // Check for amount pattern
        val hasAmount = AmountParser.extractAmount(smsBody) != null
        
        return (hasBankSender || hasTransactionKeywords) && hasAmount
    }
}

