# Technical Specifications - SMS Reader & AI Classifier

## 📱 Application Details

### Basic Information
- **App Name**: SMS Finance Tracker
- **Package Name**: com.smsreader.financetracker
- **Version**: 1.0.0
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

---

## 🏛️ Architecture

### MVVM + Clean Architecture
```
app/
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── TransactionDao.kt
│   │   │   └── WalletDao.kt
│   │   └── entities/
│   │       ├── Transaction.kt
│   │       └── Wallet.kt
│   └── repository/
│       ├── TransactionRepository.kt
│       └── WalletRepository.kt
├── domain/
│   ├── classifier/
│   │   ├── AmountParser.kt
│   │   ├── MerchantExtractor.kt
│   │   ├── CategoryClassifier.kt
│   │   └── WalletDetector.kt
│   └── models/
│       ├── ClassificationResult.kt
│       └── SmsAnalysis.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── screens/
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt
│   │   │   └── DashboardViewModel.kt
│   │   ├── wallets/
│   │   │   ├── WalletsScreen.kt
│   │   │   └── WalletsViewModel.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt
│   │   │   └── SettingsViewModel.kt
│   │   └── permission/
│   │       └── PermissionScreen.kt
│   └── components/
│       ├── charts/
│       │   ├── PieChart.kt
│       │   └── BarChart.kt
│       └── TransactionCard.kt
├── service/
│   └── SmsReceiver.kt
└── MainActivity.kt
```

---

## 🗄️ Database Schema (Room)

### Transaction Entity
```kotlin
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["walletId"]),
        Index(value = ["category"]),
        Index(value = ["timestamp"])
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "currency")
    val currency: String = "INR",
    
    @ColumnInfo(name = "category")
    val category: String,
    
    @ColumnInfo(name = "merchant")
    val merchant: String,
    
    @ColumnInfo(name = "wallet_id")
    val walletId: String,
    
    @ColumnInfo(name = "transaction_type")
    val transactionType: TransactionType,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "sms_body")
    val smsBody: String,
    
    @ColumnInfo(name = "sender_id")
    val senderId: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    DEBIT, CREDIT
}
```

### Wallet Entity
```kotlin
@Entity(
    tableName = "wallets",
    indices = [Index(value = ["walletId"], unique = true)]
)
data class Wallet(
    @PrimaryKey
    @ColumnInfo(name = "wallet_id")
    val walletId: String,
    
    @ColumnInfo(name = "detected_name")
    val detectedName: String,
    
    @ColumnInfo(name = "custom_name")
    val customName: String? = null,
    
    @ColumnInfo(name = "bank_name")
    val bankName: String? = null,
    
    @ColumnInfo(name = "card_type")
    val cardType: CardType = CardType.UNKNOWN,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_used")
    val lastUsed: Long = System.currentTimeMillis()
)

enum class CardType {
    CREDIT_CARD, DEBIT_CARD, UPI, BANK_ACCOUNT, WALLET, UNKNOWN
}
```

### DAO Interfaces
```kotlin
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE wallet_id = :walletId ORDER BY timestamp DESC")
    fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long
    
    @Query("DELETE FROM transactions")
    suspend fun clearAll()
    
    @Query("SELECT SUM(amount) FROM transactions WHERE transaction_type = 'DEBIT' AND timestamp >= :startTime")
    fun getTotalSpent(startTime: Long): Flow<Double?>
    
    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE transaction_type = 'DEBIT' AND timestamp >= :startTime GROUP BY category")
    fun getSpendingByCategory(startTime: Long): Flow<List<CategorySpending>>
}

data class CategorySpending(
    val category: String,
    val total: Double
)

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY last_used DESC")
    fun getAllWallets(): Flow<List<Wallet>>
    
    @Query("SELECT * FROM wallets WHERE wallet_id = :walletId")
    suspend fun getWalletById(walletId: String): Wallet?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallet(wallet: Wallet): Long
    
    @Update
    suspend fun updateWallet(wallet: Wallet)
    
    @Query("UPDATE wallets SET custom_name = :customName WHERE wallet_id = :walletId")
    suspend fun updateCustomName(walletId: String, customName: String)
    
    @Query("DELETE FROM wallets")
    suspend fun clearAll()
}
```

---

## 🤖 AI Classification Implementation

### 1. Amount Parser
```kotlin
object AmountParser {
    private val amountPatterns = listOf(
        // Rs.1,234.56 or Rs 1234.56 or Rs1234
        Regex("""Rs\.?\s?(\d+(?:,\d+)*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        // INR 1234.56
        Regex("""INR\s?(\d+(?:,\d+)*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        // 1,234.56 INR
        Regex("""(\d+(?:,\d+)*(?:\.\d{2})?)\s?INR""", RegexOption.IGNORE_CASE),
        // Debited/Credited for Rs.1234
        Regex("""(?:debited|credited|spent|paid).*?Rs\.?\s?(\d+(?:,\d+)*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
    )
    
    fun extractAmount(smsBody: String): Double? {
        for (pattern in amountPatterns) {
            val match = pattern.find(smsBody)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                return amountStr.toDoubleOrNull()
            }
        }
        return null
    }
}
```

### 2. Merchant Extractor
```kotlin
object MerchantExtractor {
    private val merchantPatterns = listOf(
        // "at MERCHANT"
        Regex("""at\s+([A-Z0-9\s\-\.]+?)(?:\s+on|\s+for|\s+via|$)""", RegexOption.IGNORE_CASE),
        // "to MERCHANT"
        Regex("""to\s+([A-Z0-9\s\-\.]+?)(?:\s+on|\s+for|\s+via|$)""", RegexOption.IGNORE_CASE),
        // "from MERCHANT"
        Regex("""from\s+([A-Z0-9\s\-\.]+?)(?:\s+on|\s+for|\s+via|$)""", RegexOption.IGNORE_CASE),
        // "on MERCHANT"
        Regex("""on\s+([A-Z0-9\s\-\.]+?)(?:\s+for|\s+via|$)""", RegexOption.IGNORE_CASE)
    )
    
    fun extractMerchant(smsBody: String): String {
        for (pattern in merchantPatterns) {
            val match = pattern.find(smsBody)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return "Unknown"
    }
}
```

### 3. Category Classifier
```kotlin
object CategoryClassifier {
    private val categoryKeywords = mapOf(
        "Food & Dining" to listOf(
            "swiggy", "zomato", "restaurant", "cafe", "food", "domino", "pizza",
            "burger", "starbucks", "dunkin", "kfc", "mcdonald", "subway", "bakery",
            "dhaba", "biryani", "eatery", "kitchen", "dine", "buffet"
        ),
        "Transportation" to listOf(
            "uber", "ola", "rapido", "petrol", "diesel", "parking", "toll", "fuel",
            "gas", "station", "metro", "transit", "cab", "taxi", "auto", "bus",
            "railway", "ticket", "transport"
        ),
        "Health & Wellness" to listOf(
            "pharmacy", "medicine", "hospital", "clinic", "doctor", "medical",
            "health", "apollo", "medplus", "gym", "fitness", "yoga", "wellness",
            "diagnostic", "lab", "test", "checkup", "cure", "medico"
        ),
        "Shopping" to listOf(
            "amazon", "flipkart", "myntra", "shopping", "mall", "store", "retail",
            "mart", "shop", "meesho", "ajio", "lifestyle", "westside", "fashion",
            "clothing", "electronics", "gadget"
        ),
        "Groceries" to listOf(
            "bigbasket", "grofers", "blinkit", "instamart", "zepto", "supermarket",
            "grocery", "kirana", "vegetables", "fruits", "dairy", "milk", "fresh",
            "reliance fresh", "dmart", "more"
        ),
        "Utilities & Bills" to listOf(
            "electricity", "water", "gas", "internet", "broadband", "mobile",
            "recharge", "bill", "payment", "utility", "bsnl", "airtel", "jio",
            "vodafone", "tata", "subscription", "postpaid", "prepaid"
        ),
        "Entertainment" to listOf(
            "movie", "cinema", "pvr", "inox", "netflix", "spotify", "prime",
            "hotstar", "disney", "youtube", "gaming", "steam", "playstation",
            "xbox", "entertainment", "concert", "show", "theatre"
        ),
        "Education" to listOf(
            "school", "college", "university", "course", "tuition", "book",
            "academy", "institute", "education", "learning", "udemy", "coursera",
            "byju", "unacademy", "class", "fee", "exam"
        ),
        "Travel" to listOf(
            "hotel", "flight", "airbnb", "oyo", "booking", "makemytrip", "goibibo",
            "travel", "ticket", "railway", "irctc", "airways", "airlines", "stay",
            "resort", "vacation", "trip", "tour"
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
                    score += keyword.length // Longer keywords get higher priority
                }
            }
            
            if (score > maxScore) {
                maxScore = score
                bestCategory = category
            }
        }
        
        return bestCategory
    }
}
```

### 4. Wallet Detector
```kotlin
object WalletDetector {
    private val walletPatterns = listOf(
        // **1234 or ****1234
        Regex("""\*+(\d{4})"""),
        // XX1234
        Regex("""XX(\d{4})"""),
        // ending 1234 or ending in 1234
        Regex("""ending\s+(?:in\s+)?(\d{4})""", RegexOption.IGNORE_CASE),
        // A/C **1234
        Regex("""A/?C\s+\*+(\d{4})""", RegexOption.IGNORE_CASE)
    )
    
    fun detectWallet(smsBody: String, senderId: String): String? {
        for (pattern in walletPatterns) {
            val match = pattern.find(smsBody)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }
    
    fun generateDetectedName(walletId: String, senderId: String): String {
        val bankName = extractBankName(senderId)
        return if (bankName != null) {
            "$bankName - XX$walletId"
        } else {
            "Card XX$walletId"
        }
    }
    
    private fun extractBankName(senderId: String): String? {
        val bankPatterns = mapOf(
            "HDFC" to listOf("HDFC", "HDFCBK"),
            "ICICI" to listOf("ICICI"),
            "SBI" to listOf("SBI", "SBICARD"),
            "AXIS" to listOf("AXIS"),
            "KOTAK" to listOf("KOTAK"),
            "PAYTM" to listOf("PAYTM"),
            "PHONEPE" to listOf("PHONEPE"),
            "GPAY" to listOf("GPAY", "GOOGLEPAY")
        )
        
        for ((bank, patterns) in bankPatterns) {
            if (patterns.any { senderId.contains(it, ignoreCase = true) }) {
                return bank
            }
        }
        return null
    }
}
```

### 5. Transaction Type Detector
```kotlin
object TransactionTypeDetector {
    private val debitKeywords = listOf(
        "debited", "spent", "paid", "withdrawn", "purchase", "deducted", "charged"
    )
    
    private val creditKeywords = listOf(
        "credited", "received", "deposited", "refund", "cashback", "earned"
    )
    
    fun detectType(smsBody: String): TransactionType {
        val lowerBody = smsBody.lowercase()
        
        for (keyword in debitKeywords) {
            if (lowerBody.contains(keyword)) {
                return TransactionType.DEBIT
            }
        }
        
        for (keyword in creditKeywords) {
            if (lowerBody.contains(keyword)) {
                return TransactionType.CREDIT
            }
        }
        
        return TransactionType.DEBIT // Default assumption
    }
}
```

---

## 📱 SMS Receiver Implementation

### BroadcastReceiver
```kotlin
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>
                for (pdu in pdus) {
                    val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                    val senderId = sms.originatingAddress ?: continue
                    val smsBody = sms.messageBody ?: continue
                    
                    // Process SMS in background
                    CoroutineScope(Dispatchers.IO).launch {
                        processSms(context, senderId, smsBody)
                    }
                }
            }
        }
    }
    
    private suspend fun processSms(context: Context, senderId: String, smsBody: String) {
        // Check if it's a transaction SMS
        val amount = AmountParser.extractAmount(smsBody) ?: return
        val transactionType = TransactionTypeDetector.detectType(smsBody)
        
        // Only process debit transactions for spending tracking
        if (transactionType != TransactionType.DEBIT) return
        
        val merchant = MerchantExtractor.extractMerchant(smsBody)
        val category = CategoryClassifier.classify(merchant, smsBody)
        val walletId = WalletDetector.detectWallet(smsBody, senderId) ?: "UNKNOWN"
        
        // Save to database
        val database = AppDatabase.getInstance(context)
        val transaction = Transaction(
            amount = amount,
            category = category,
            merchant = merchant,
            walletId = walletId,
            transactionType = transactionType,
            timestamp = System.currentTimeMillis(),
            smsBody = smsBody,
            senderId = senderId
        )
        database.transactionDao().insertTransaction(transaction)
        
        // Create or update wallet
        val existingWallet = database.walletDao().getWalletById(walletId)
        if (existingWallet == null) {
            val wallet = Wallet(
                walletId = walletId,
                detectedName = WalletDetector.generateDetectedName(walletId, senderId),
                bankName = WalletDetector.extractBankName(senderId)
            )
            database.walletDao().insertWallet(wallet)
        }
        
        // Show notification
        showTransactionNotification(context, transaction)
    }
}
```

---

## 🎨 UI Components (Jetpack Compose)

### Dashboard Screen Structure
```kotlin
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val transactions by viewModel.transactions.collectAsState()
    val categorySpending by viewModel.categorySpending.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    
    Scaffold(
        topBar = { DashboardTopBar() },
        bottomBar = { BottomNavigationBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Cards
            item {
                SummaryCards(totalSpent, transactions.size)
            }
            
            // Spending by Category Chart
            item {
                CategoryPieChart(categorySpending)
            }
            
            // Recent Transactions
            item {
                Text("Recent Transactions", style = MaterialTheme.typography.titleLarge)
            }
            
            items(transactions.take(10)) { transaction ->
                TransactionCard(transaction)
            }
        }
    }
}
```

### Material Design 3 Theme
```kotlin
@Composable
fun SMSReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## 🔧 Build Configuration

### build.gradle (Project Level)
```gradle
plugins {
    id 'com.android.application' version '8.1.0' apply false
    id 'com.android.library' version '8.1.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.0' apply false
    id 'com.google.devtools.ksp' version '1.9.0-1.0.13' apply false
}
```

### build.gradle (App Level)
```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.devtools.ksp'
}

android {
    namespace 'com.smsreader.financetracker'
    compileSdk 34

    defaultConfig {
        applicationId "com.smsreader.financetracker"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }

    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = '17'
    }
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.1'
    }
}

dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    
    // Jetpack Compose
    implementation platform('androidx.compose:compose-bom:2024.02.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.activity:activity-compose:1.8.2'
    implementation 'androidx.navigation:navigation-compose:2.7.6'
    
    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    ksp 'androidx.room:room-compiler:2.6.1'
    
    // Charts
    implementation 'com.patrykandpatrick.vico:compose:1.13.1'
    implementation 'com.patrykandpatrick.vico:compose-m3:1.13.1'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    
    // Debug
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
}
```

---

## 📋 AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- SMS Permissions -->
    <uses-permission android:name="android.permission.READ_SMS" />
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SMSReader"
        tools:targetApi="31">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SMSReader">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- SMS Receiver -->
        <receiver
            android:name=".service.SmsReceiver"
            android:exported="true"
            android:permission="android.permission.BROADCAST_SMS">
            <intent-filter android:priority="2147483647">
                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

---

## 🚀 GitHub Actions Workflow

### .github/workflows/android-build.yml
```yaml
name: Android Build

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle
      
      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
      
      - name: Build Debug APK
        run: ./gradlew assembleDebug
      
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

---

## 📊 Spending Categories (Final List)

1. **Food & Dining** 🍔
2. **Transportation** 🚗
3. **Health & Wellness** 💊
4. **Shopping** 🛍️
5. **Groceries** 🛒
6. **Utilities & Bills** 💡
7. **Entertainment** 🎬
8. **Education** 📚
9. **Travel** ✈️
10. **Miscellaneous** 📦

---

## ✅ Testing Strategy

### Unit Tests
- Amount parser with various formats
- Merchant extractor accuracy
- Category classification precision
- Wallet detection patterns

### Integration Tests
- SMS receiver → Database flow
- ViewModel → UI state updates
- Wallet rename propagation

### Manual Testing
- Test with 10 real bank SMS
- Verify dashboard calculations
- Test debug scanner with filters
- Verify clear data functionality

---

## 📦 Deliverables

1. ✅ Complete Android project source code
2. ✅ GitHub Actions workflow for APK building
3. ✅ README with setup instructions
4. ✅ Sample SMS data for testing
5. ✅ APK file (via GitHub Actions artifact)

---

**Ready for approval and implementation!**

