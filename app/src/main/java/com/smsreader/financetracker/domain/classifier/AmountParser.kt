package com.smsreader.financetracker.domain.classifier

object AmountParser {
    private val amountPatterns = listOf(
        // Rs.1,234.56 or Rs 1234.56 or Rs1234
        Regex("""Rs\.?\s?(\d+(?:,\d+)*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        // INR 1234.56
        Regex("""INR\s?(\d+(?:,\d+)*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        // 1,234.56 INR
        Regex("""(\d+(?:,\d+)*(?:\.\d{2})?)\s?INR""", RegexOption.IGNORE_CASE),
        // Debited/Credited for Rs.1234
        Regex("""(?:debited|credited|spent|paid).*?Rs\.?\s?(\d+(?:,\d+)*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        // Amount: Rs.1234 or Amt: Rs.1234
        Regex("""(?:amount|amt)[:.\s]+Rs\.?\s?(\d+(?:,\d+)*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
    )

    fun extractAmount(smsBody: String): Double? {
        for (pattern in amountPatterns) {
            val match = pattern.find(smsBody)
            if (match != null && match.groupValues.size > 1) {
                val amountStr = match.groupValues[1].replace(",", "")
                val amount = amountStr.toDoubleOrNull()
                // Validate reasonable amount range (0.01 to 10,00,000)
                if (amount != null && amount > 0.0 && amount <= 1000000.0) {
                    return amount
                }
            }
        }
        return null
    }
}

