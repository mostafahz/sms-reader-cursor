# SMS Reader & AI Classifier - Project Plan

## 🎯 Project Overview
An Android application that reads SMS messages, classifies them using on-device AI, and provides spending analytics through a beautiful dashboard.

---

## 📋 Requirements Summary

### Core Requirements
1. **SMS Reading**: Read all incoming SMS messages
2. **On-Device AI Classification**: Parse and categorize SMS without internet
3. **Spending Tracking**: Track expenses by category and wallet
4. **Wallet Management**: Auto-detect cards/accounts and allow custom naming
5. **Testing Tools**: Scan specific senders with message limits
6. **Data Management**: Clear all data capability

### Technical Constraints
- **Distribution**: Internal use (sideloading)
- **AI**: On-device only (no cloud/internet required)
- **Tech Stack**: Kotlin + Jetpack Compose + Material Design 3
- **Build**: GitHub Actions (no local Android Studio required)
- **Permissions**: READ_SMS, RECEIVE_SMS

---

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Android Application                     │
├─────────────────────────────────────────────────────────────┤
│  UI Layer (Jetpack Compose + Material Design 3)             │
│  ├─ Dashboard Screen (Charts, Recent Transactions)          │
│  ├─ Wallets Screen (List, Rename, Auto-detect)              │
│  ├─ Settings/Debug Screen (Scan, Clear Data)                │
│  └─ Navigation (Bottom Nav Bar)                             │
├─────────────────────────────────────────────────────────────┤
│  Business Logic Layer                                        │
│  ├─ SMS Listener (Background Service)                       │
│  ├─ AI Classifier (Regex + Keyword Matcher)                 │
│  ├─ Wallet Detector (Pattern Recognition)                   │
│  └─ Analytics Engine (Aggregation, Grouping)                │
├─────────────────────────────────────────────────────────────┤
│  Data Layer (Room Database + Repository Pattern)            │
│  ├─ Transaction Entity (amount, category, wallet, date)     │
│  ├─ Wallet Entity (id, detected_name, custom_name)          │
│  └─ Raw SMS Cache (for debugging/reprocessing)              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📱 Screen Breakdown

### 1. Dashboard Screen (Home)
**Purpose**: Overview of spending patterns

**Components**:
- Pie Chart: Spending by Category (current month)
- Bar Chart: Spending by Wallet (current month)
- Summary Cards: Total Spent, Transaction Count, Top Category
- Recent Transactions List (last 10 transactions)
- Date Filter: This Month, Last 3 Months, All Time

### 2. Wallets Screen
**Purpose**: Manage card/account identifiers

**Components**:
- List of detected wallets
- Each wallet shows:
  - Auto-detected name (e.g., "Card ending in 1234")
  - Custom name (editable, e.g., "Personal Visa")
  - Transaction count
  - Total spent
- "Edit" button to rename wallets
- Auto-detection runs on each new SMS

### 3. Settings/Debug Screen
**Purpose**: Testing and data management

**Components**:
- **SMS Scanner Section**:
  - Dropdown: Select Sender ID (e.g., "TX-BANKNAME")
  - Number Input: Count of messages to scan (1-100)
  - "Scan Now" button
  - Progress indicator during scan
  - Results summary (X messages scanned, Y transactions found)
  
- **Data Management Section**:
  - "Clear All Data" button (with confirmation dialog)
  - Storage info (database size, message count)

- **App Info Section**:
  - Version number
  - Permissions status (SMS, Background)
  - About/Credits

### 4. Permissions Screen (First Launch)
**Purpose**: Request necessary permissions

**Components**:
- Explanation of why SMS access is needed
- Visual guide showing app functionality
- "Grant Permissions" button

---

## 🤖 AI Classification System

### Approach: Hybrid Rule-Based + Pattern Matching

#### Step 1: SMS Parsing (Regex)
Extract structured data from SMS:
- **Amount**: `Rs.? ?(\d+(?:,\d+)*(?:\.\d{2})?)`
- **Merchant/Vendor**: Text after "at", "to", "from", "for"
- **Card Identifier**: "XX1234", "****1234", "Card ending 1234"
- **Transaction Type**: "debited", "credited", "spent", "paid"

#### Step 2: Category Classification (Keyword Matching)
Map merchant names to categories using keyword lists:

**Food & Dining**
- Keywords: swiggy, zomato, restaurant, cafe, food, domino, pizza, burger, starbucks, dunkin, kfc, mcdonald
- Weight: High priority for food delivery apps

**Transportation**
- Keywords: uber, ola, rapido, petrol, diesel, parking, toll, fuel, gas, station, metro, transit

**Health & Wellness**
- Keywords: pharmacy, medicine, hospital, clinic, doctor, medical, health, apollo, medplus, gym, fitness

**Shopping**
- Keywords: amazon, flipkart, myntra, shopping, mall, store, retail

**Groceries**
- Keywords: bigbasket, grofers, blinkit, instamart, supermarket, grocery, kirana

**Utilities & Bills**
- Keywords: electricity, water, gas, internet, broadband, mobile, recharge, bill, payment

**Entertainment**
- Keywords: movie, cinema, pvr, netflix, spotify, prime, hotstar, subscription, gaming

**Education**
- Keywords: school, college, university, course, tuition, book, academy

**Travel**
- Keywords: hotel, flight, airbnb, booking, travel, ticket, railway, irctc

**Miscellaneous**
- Default fallback for unmatched transactions

#### Step 3: Wallet Detection
Auto-detect card/account from SMS content:
- Pattern: `\*+(\d{4})` or `XX(\d{4})` or `ending (\d{4})`
- Create unique wallet ID from last 4 digits
- Store mapping in database
- Allow user to rename later

---

## 🗄️ Database Schema

### Transaction Table
```kotlin
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String = "INR",
    val category: String,
    val merchant: String,
    val walletId: String,
    val transactionType: String, // DEBIT or CREDIT
    val timestamp: Long,
    val smsBody: String, // Store original SMS for debugging
    val senderId: String
)
```

### Wallet Table
```kotlin
@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey val walletId: String, // e.g., "XX1234"
    val detectedName: String, // e.g., "Card ending in 1234"
    val customName: String? = null, // User-defined name
    val bankName: String? = null, // Extracted from sender ID
    val createdAt: Long
)
```

---

## 🔄 User Flows

### Flow 1: First Launch
1. User opens app
2. Permission screen appears
3. User grants READ_SMS and RECEIVE_SMS
4. App shows empty dashboard with "Scan SMS" prompt
5. User navigates to Settings → Scans existing messages
6. Dashboard populates with data

### Flow 2: Incoming SMS
1. New SMS arrives
2. Background service intercepts
3. AI classifier analyzes content
4. If transaction detected → Save to database
5. Notification: "New transaction: Rs.500 at Swiggy (Food)"
6. Dashboard updates in real-time

### Flow 3: Rename Wallet
1. User opens Wallets screen
2. Sees "Card XX1234 (HDFC Bank)"
3. Taps "Edit" icon
4. Enters custom name: "Personal Visa"
5. Saves → All past transactions update with new name

### Flow 4: Debug/Testing
1. User opens Settings
2. Selects sender: "TX-HDFCBANK"
3. Sets count: 10
4. Taps "Scan Now"
5. Progress bar shows processing
6. Results: "Scanned 10 messages, found 7 transactions"
7. Dashboard reflects new data

### Flow 5: Clear Data
1. User opens Settings
2. Taps "Clear All Data"
3. Confirmation dialog: "This will delete all transactions and wallets. Continue?"
4. User confirms
5. Database cleared
6. App returns to empty state

---

## 🚀 Development Phases

### Phase 1: Project Setup (Day 1)
- [ ] Initialize Android project (Kotlin + Compose)
- [ ] Setup Room database
- [ ] Configure GitHub Actions workflow
- [ ] Setup Material Design 3 theme
- [ ] Create navigation structure

### Phase 2: Core Functionality (Day 2-3)
- [ ] Implement SMS receiver
- [ ] Build AI classifier (regex + keywords)
- [ ] Create wallet detector
- [ ] Setup database repositories
- [ ] Write unit tests for classifiers

### Phase 3: UI Development (Day 4-5)
- [ ] Dashboard screen with charts
- [ ] Wallets screen with rename feature
- [ ] Settings/Debug screen
- [ ] Permission handling
- [ ] Material Design 3 styling

### Phase 4: Testing & Polish (Day 6)
- [ ] Test with real SMS samples
- [ ] Debug scan functionality
- [ ] Optimize performance
- [ ] Add error handling
- [ ] Final UI polish

---

## 📦 Dependencies

### Core
- Kotlin 1.9+
- Jetpack Compose BOM 2024.02.00+
- Material 3
- Android SDK 34 (target), 26+ (minimum)

### Database
- Room 2.6+
- Kotlin Coroutines

### Charts
- Vico (Modern Compose charts)
- Or MPAndroidChart with Compose wrapper

### Build
- Gradle 8.0+
- GitHub Actions for APK build

---

## 🔐 Security & Privacy

### Data Storage
- All data stored locally (Room database)
- No cloud sync or external transmission
- Database encrypted using SQLCipher (optional)

### Permissions
- READ_SMS: Required to read existing messages
- RECEIVE_SMS: Required to intercept new messages
- POST_NOTIFICATIONS: For transaction alerts (Android 13+)

### Privacy Features
- Original SMS body stored only for debugging
- Option to clear all data anytime
- No analytics or tracking
- No internet permission required

---

## ✅ Success Criteria

1. ✅ App successfully reads incoming SMS
2. ✅ AI correctly classifies 90%+ of bank transaction SMS
3. ✅ Dashboard displays accurate spending analytics
4. ✅ Wallet auto-detection works for major Indian banks
5. ✅ User can rename wallets and see changes reflected
6. ✅ Debug scanner processes specific sender SMS
7. ✅ Clear data function works without crashes
8. ✅ APK builds successfully via GitHub Actions
9. ✅ App runs smoothly without Android Studio

---

## 🎨 Design Mockup Reference

### Color Scheme (Material Design 3)
- **Primary**: Dynamic (from wallpaper) or Purple/Blue
- **Surface**: Adaptive background
- **Success/Income**: Green (#4CAF50)
- **Error/Expense**: Red (#F44336)
- **Categories**: Distinct colors for each category

### Typography
- **Headlines**: Roboto Bold
- **Body**: Roboto Regular
- **Numbers**: Roboto Mono (for amounts)

---

## 📝 Next Steps

1. **Review this plan** - Confirm all requirements are captured
2. **Approve spending categories** - Review the category list
3. **Confirm specifications** - Check technical details
4. **Begin implementation** - Start Phase 1

---

**Estimated Timeline**: 6 days for full implementation
**Team Size**: 1 developer
**Platform**: Android 8.0+ (API 26+)

