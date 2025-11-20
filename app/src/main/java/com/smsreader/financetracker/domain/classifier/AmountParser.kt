package com.smsreader.financetracker.domain.classifier

object AmountParser {
    private val amountPatterns = listOf(
        // Rs.1,234.56 or Rs 1234.56 or Rs1234
        Regex("""Rs\.?\s?(\d+(?:,\d+)*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        // INR 1234.56
        Regex("""INR\s?(\d+(?:,\d+)*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        // 1,234.56 INR
        Regex("""(\d+(?:,\d+)*(?:\.\d{1,2})?)\s?INR""", RegexOption.IGNORE_CASE),
        // AED 1234.56 or AED 1234.5 or AED1234
        Regex("""AED\s?(\d+(?:,\d+)*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        // 1,234.56 AED or 1234.5 AED
        Regex("""(\d+(?:,\d+)*(?:\.\d{1,2})?)\s?AED""", RegexOption.IGNORE_CASE),
        // Debited/Credited for Rs.1234 or AED 1234
        Regex("""(?:debited|credited|spent|paid|payment|transaction).*?(?:Rs\.?\s?|AED\s?)(\d+(?:,\d+)*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        // Amount: Rs.1234 or Amt: AED 1234
        Regex("""(?:amount|amt)[:.\s]+(?:Rs\.?\s?|AED\s?)(\d+(?:,\d+)*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        // "of AED 85.36" or "of Rs 500"
        Regex("""of\s+(?:AED|Rs\.?)\s?(\d+(?:,\d+)*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
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

