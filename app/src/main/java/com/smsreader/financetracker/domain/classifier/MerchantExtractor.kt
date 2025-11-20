package com.smsreader.financetracker.domain.classifier

object MerchantExtractor {
    private val merchantPatterns = listOf(
        // "at MERCHANT"
        Regex("""\bat\s+([A-Z0-9\s\-\.]+?)(?:\s+on|\s+for|\s+via|\s+with|\.|$)""", RegexOption.IGNORE_CASE),
        // "to MERCHANT"
        Regex("""\bto\s+([A-Z0-9\s\-\.]+?)(?:\s+on|\s+for|\s+via|\s+with|\.|$)""", RegexOption.IGNORE_CASE),
        // "from MERCHANT"
        Regex("""\bfrom\s+([A-Z0-9\s\-\.]+?)(?:\s+on|\s+for|\s+via|\s+with|\.|$)""", RegexOption.IGNORE_CASE),
        // "on MERCHANT"
        Regex("""\bon\s+([A-Z0-9\s\-\.]+?)(?:\s+for|\s+via|\s+with|\s+using|\.|$)""", RegexOption.IGNORE_CASE),
        // "for MERCHANT"
        Regex("""\bfor\s+([A-Z0-9\s\-\.]+?)(?:\s+on|\s+via|\s+with|\s+using|\.|$)""", RegexOption.IGNORE_CASE)
    )

    fun extractMerchant(smsBody: String): String {
        for (pattern in merchantPatterns) {
            val match = pattern.find(smsBody)
            if (match != null && match.groupValues.size > 1) {
                val merchant = match.groupValues[1].trim()
                // Remove common suffixes and clean up
                val cleaned = merchant
                    .replace(Regex("""\s+"""), " ")
                    .trim()
                
                if (cleaned.isNotBlank() && cleaned.length >= 3) {
                    return cleaned
                }
            }
        }
        return "Unknown"
    }
}

