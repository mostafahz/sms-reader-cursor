package com.smsreader.financetracker.domain.classifier

import com.smsreader.financetracker.data.local.entities.CardType

object WalletDetector {
    private val cardPatterns = listOf(
        // **1234 or ****1234
        Regex("""\*{2,}(\d{4})"""),
        // XX1234
        Regex("""\bXX(\d{4})\b"""),
        // ending 1234 or ending in 1234
        Regex("""ending\s+(?:in\s+)?(\d{4})""", RegexOption.IGNORE_CASE),
        // A/C **1234 or A/c **1234
        Regex("""A/?[Cc]\s+\*{2,}(\d{4})"""),
        // Card XX1234
        Regex("""[Cc]ard\s+(?:XX)?(\d{4})""")
    )

    private val upiPatterns = listOf(
        // username@bank
        Regex("""([a-zA-Z0-9._-]+@[a-zA-Z]+)"""),
        // UPI ID: username@bank
        Regex("""UPI\s+ID[:.\s]+([a-zA-Z0-9._-]+@[a-zA-Z]+)""", RegexOption.IGNORE_CASE),
        // UPI ref with phone number (fallback)
        Regex("""UPI.*?(\d{10})""", RegexOption.IGNORE_CASE)
    )

    fun detectWallet(smsBody: String, senderId: String): String? {
        // First try to detect card numbers
        for (pattern in cardPatterns) {
            val match = pattern.find(smsBody)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1]
            }
        }

        // Then try UPI IDs
        for (pattern in upiPatterns) {
            val match = pattern.find(smsBody)
            if (match != null && match.groupValues.size > 1) {
                val upiId = match.groupValues[1]
                // Validate UPI ID format
                if (upiId.contains("@")) {
                    return "UPI_${upiId.take(20)}" // Truncate long UPI IDs
                }
                // For phone numbers, just use last 4 digits
                if (upiId.length == 10) {
                    return "UPI_${upiId.takeLast(4)}"
                }
            }
        }

        // Fallback: try to extract any bank account reference
        val bankAccountPattern = Regex("""(?:account|a/c).*?(\d{4})""", RegexOption.IGNORE_CASE)
        val accountMatch = bankAccountPattern.find(smsBody)
        if (accountMatch != null && accountMatch.groupValues.size > 1) {
            return accountMatch.groupValues[1]
        }

        return null
    }

    fun generateDetectedName(walletId: String, senderId: String): String {
        val bankName = extractBankName(senderId)
        
        return when {
            walletId.startsWith("UPI_") -> {
                val upiPart = walletId.removePrefix("UPI_")
                if (bankName != null) {
                    "$bankName UPI - $upiPart"
                } else {
                    "UPI - $upiPart"
                }
            }
            bankName != null -> "$bankName - XX$walletId"
            else -> "Card XX$walletId"
        }
    }

    fun detectCardType(walletId: String, smsBody: String, senderId: String): CardType {
        val lowerBody = smsBody.lowercase()
        val lowerSender = senderId.lowercase()

        return when {
            walletId.startsWith("UPI_") -> CardType.UPI
            lowerBody.contains("credit card") || lowerSender.contains("credit") -> CardType.CREDIT_CARD
            lowerBody.contains("debit card") || lowerBody.contains("debit") -> CardType.DEBIT_CARD
            lowerBody.contains("wallet") || lowerSender.contains("paytm") || 
            lowerSender.contains("phonepe") || lowerSender.contains("gpay") -> CardType.WALLET
            lowerBody.contains("account") || lowerBody.contains("a/c") -> CardType.BANK_ACCOUNT
            else -> CardType.UNKNOWN
        }
    }

    fun extractBankName(senderId: String): String? {
        val bankPatterns = mapOf(
            "HDFC" to listOf("HDFC", "HDFCBK", "HDFCBANK"),
            "ICICI" to listOf("ICICI", "ICICIB"),
            "SBI" to listOf("SBI", "SBICARD", "SBIINB"),
            "AXIS" to listOf("AXIS", "AXISBK"),
            "KOTAK" to listOf("KOTAK", "KOTAKB"),
            "Paytm" to listOf("PAYTM", "PYTM"),
            "PhonePe" to listOf("PHONEPE", "PHONPE", "PNPE"),
            "Google Pay" to listOf("GPAY", "GOOGLEPAY", "GOGLPAY"),
            "IDFC" to listOf("IDFC", "IDFCFB"),
            "Yes Bank" to listOf("YESBNK", "YESBANK"),
            "IndusInd" to listOf("INDUS", "INDUSIND"),
            "Standard Chartered" to listOf("SCBANK", "STANCH"),
            "Citibank" to listOf("CITI", "CITIBNK"),
            "HSBC" to listOf("HSBC"),
            "Bank of Baroda" to listOf("BOB", "BARODA"),
            "Punjab National Bank" to listOf("PNB", "PNBSMS"),
            "Canara Bank" to listOf("CANARA", "CNRBNK"),
            "Union Bank" to listOf("UNIONBK", "UNION"),
            "Bank of India" to listOf("BOI", "BOISMS")
        )

        val upperSender = senderId.uppercase()
        for ((bank, patterns) in bankPatterns) {
            if (patterns.any { upperSender.contains(it) }) {
                return bank
            }
        }
        return null
    }
}

