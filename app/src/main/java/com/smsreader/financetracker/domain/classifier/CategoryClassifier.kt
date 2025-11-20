package com.smsreader.financetracker.domain.classifier

object CategoryClassifier {
    private val categoryKeywords = mapOf(
        "Food & Dining" to listOf(
            "swiggy", "zomato", "restaurant", "cafe", "food", "domino", "pizza",
            "burger", "starbucks", "dunkin", "kfc", "mcdonald", "subway", "bakery",
            "dhaba", "biryani", "eatery", "kitchen", "dine", "buffet", "mcd",
            "pizzahut", "burgerking", "wendys", "taco", "dining", "foodpanda"
        ),
        "Transportation" to listOf(
            "uber", "ola", "rapido", "petrol", "diesel", "parking", "toll", "fuel",
            "gas", "station", "metro", "transit", "cab", "taxi", "auto", "bus",
            "railway", "transport", "fastag", "hp", "bharat petroleum", "indian oil",
            "bpcl", "iocl", "shell", "essar", "reliance petrol"
        ),
        "Health & Wellness" to listOf(
            "pharmacy", "medicine", "hospital", "clinic", "doctor", "medical",
            "health", "apollo", "medplus", "gym", "fitness", "yoga", "wellness",
            "diagnostic", "lab", "test", "checkup", "cure", "medico", "pharma",
            "cult.fit", "cultfit", "healthify", "practo", "1mg"
        ),
        "Shopping" to listOf(
            "amazon", "flipkart", "myntra", "shopping", "mall", "store", "retail",
            "mart", "shop", "meesho", "ajio", "lifestyle", "westside", "fashion",
            "clothing", "electronics", "gadget", "shoppe", "snapdeal", "paytm mall",
            "tatacliq", "nykaa", "shoppers stop", "max fashion", "reliance trends"
        ),
        "Groceries" to listOf(
            "bigbasket", "grofers", "blinkit", "instamart", "zepto", "supermarket",
            "grocery", "kirana", "vegetables", "fruits", "dairy", "milk", "fresh",
            "reliance fresh", "dmart", "more", "spencers", "nature's basket",
            "jiomart", "dunzo", "milkbasket", "licious", "freshmenu"
        ),
        "Utilities & Bills" to listOf(
            "electricity", "water", "gas", "internet", "broadband", "mobile",
            "recharge", "bill", "payment", "utility", "bsnl", "airtel", "jio",
            "vodafone", "tata", "subscription", "postpaid", "prepaid", "vi",
            "idea", "act", "hathway", "bescom", "mseb", "electricity board"
        ),
        "Entertainment" to listOf(
            "movie", "cinema", "pvr", "inox", "netflix", "spotify", "prime",
            "hotstar", "disney", "youtube", "gaming", "steam", "playstation",
            "xbox", "entertainment", "concert", "show", "theatre", "bookmyshow",
            "paytm insider", "sony liv", "zee5", "voot", "alt balaji", "gaana"
        ),
        "Education" to listOf(
            "school", "college", "university", "course", "tuition", "book",
            "academy", "institute", "education", "learning", "udemy", "coursera",
            "byju", "unacademy", "class", "fee", "exam", "edurev", "toppr",
            "vedantu", "whitehat", "upgrad", "great learning", "simplilearn"
        ),
        "Travel" to listOf(
            "hotel", "flight", "airbnb", "oyo", "booking", "makemytrip", "goibibo",
            "travel", "ticket", "railway", "irctc", "airways", "airlines", "stay",
            "resort", "vacation", "trip", "tour", "cleartrip", "yatra", "trivago",
            "indigo", "spicejet", "air india", "vistara", "treebo", "fabhotel"
        ),
        "Miscellaneous" to listOf() // Fallback
    )

    fun classify(merchant: String, smsBody: String): String {
        val searchText = "${merchant.lowercase()} ${smsBody.lowercase()}"

        var maxScore = 0
        var bestCategory = "Miscellaneous"

        for ((category, keywords) in categoryKeywords) {
            if (category == "Miscellaneous") continue

            var score = 0
            for (keyword in keywords) {
                if (searchText.contains(keyword)) {
                    // Longer keywords get higher priority, exact matches get bonus
                    score += keyword.length * 2
                    if (merchant.lowercase() == keyword) {
                        score += 50 // Bonus for exact merchant name match
                    }
                }
            }

            if (score > maxScore) {
                maxScore = score
                bestCategory = category
            }
        }

        return bestCategory
    }

    fun getAllCategories(): List<String> {
        return categoryKeywords.keys.toList()
    }
}

