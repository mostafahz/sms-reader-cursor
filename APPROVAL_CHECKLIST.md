# 📋 Approval Checklist - SMS Finance Tracker

Please review the following items and confirm before we begin implementation.

---

## ✅ Core Features Confirmed

### 1. SMS Reading & Processing
- ✅ Read all incoming SMS automatically
- ✅ Parse transaction details (amount, merchant, card)
- ✅ On-device AI classification (no internet needed)
- ✅ Support for major Indian banks (HDFC, ICICI, SBI, Axis, Kotak, etc.)

### 2. Dashboard
- ✅ Pie Chart: Spending by Category
- ✅ Bar Chart: Spending by Wallet
- ✅ Summary Cards: Total Spent, Transaction Count
- ✅ Recent Transactions List (last 10)
- ✅ Date Filters: This Month, Last 3 Months, All Time

### 3. Wallets Management
- ✅ Auto-detect cards from SMS (e.g., "XX1234")
- ✅ Display detected name (e.g., "HDFC - XX1234")
- ✅ Allow custom renaming (e.g., "Personal Visa")
- ✅ Show transaction count and total spent per wallet
- ✅ Real-time updates across all transactions

### 4. Settings/Debug Screen
- ✅ **SMS Scanner**:
  - Select specific Sender ID
  - Choose number of messages to scan (1-100)
  - Scan button with progress indicator
  - Results summary display
- ✅ **Data Management**:
  - Clear All Data button
  - Confirmation dialog before deletion
  - Database statistics display
- ✅ **App Info**:
  - Version number
  - Permissions status

---

## 🎨 Spending Categories (10 Total)

| # | Category | Example Keywords |
|---|----------|------------------|
| 1 | **Food & Dining** 🍔 | swiggy, zomato, restaurant, cafe, pizza, burger |
| 2 | **Transportation** 🚗 | uber, ola, petrol, parking, toll, metro |
| 3 | **Health & Wellness** 💊 | pharmacy, hospital, doctor, gym, medical |
| 4 | **Shopping** 🛍️ | amazon, flipkart, myntra, mall, retail |
| 5 | **Groceries** 🛒 | bigbasket, blinkit, supermarket, vegetables |
| 6 | **Utilities & Bills** 💡 | electricity, water, internet, mobile, recharge |
| 7 | **Entertainment** 🎬 | movie, netflix, spotify, gaming, cinema |
| 8 | **Education** 📚 | school, college, course, tuition, books |
| 9 | **Travel** ✈️ | hotel, flight, booking, railway, vacation |
| 10 | **Miscellaneous** 📦 | Everything else (fallback) |

**Do you want to add, remove, or modify any categories?**

---

## 🔧 Technical Stack

### Development
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design**: Material Design 3
- **Database**: Room (SQLite)
- **Min Android Version**: Android 8.0 (API 26)
- **Target Android Version**: Android 14 (API 34)

### AI Classification (On-Device)
- **Approach**: Regex + Keyword Matching
- **No Internet Required**: ✅
- **No External Libraries**: ✅ (pure Kotlin logic)
- **Processing Speed**: Instant (<100ms per SMS)

### Build Method
- **GitHub Actions**: Automatic APK building
- **No Local Android Studio**: ✅
- **Artifact Download**: APK via GitHub Actions

**Is this tech stack acceptable?**

---

## 📱 App Screens (4 Total)

### Navigation Structure
```
Bottom Navigation Bar
├─ 🏠 Dashboard (Home)
├─ 💳 Wallets
└─ ⚙️ Settings
```

### First Launch Flow
1. Permission Request Screen
2. Explanation of SMS access
3. Grant Permissions
4. Dashboard (empty state)
5. Navigate to Settings → Scan existing SMS

**Does this navigation make sense?**

---

## 🔐 Permissions Required

| Permission | Purpose | When Requested |
|------------|---------|----------------|
| `READ_SMS` | Read existing SMS for scanning | First launch |
| `RECEIVE_SMS` | Intercept new incoming SMS | First launch |
| `POST_NOTIFICATIONS` | Show transaction alerts | First launch (Android 13+) |

**Note**: No internet permission = Complete privacy

**Are you comfortable with these permissions?**

---

## 🧪 Testing Features

### Debug Scanner
- **Purpose**: Test classification on existing SMS
- **Filters**:
  - Sender ID dropdown (populated from existing SMS)
  - Message count slider (1-100)
- **Output**: 
  - Progress bar during scan
  - Summary: "Scanned 10 messages, found 7 transactions"
  - Transactions appear in dashboard

### Clear Data
- **Purpose**: Reset app to fresh state
- **Confirmation**: "Are you sure? This will delete all transactions and wallets."
- **Action**: Clears entire database

**Do these testing features meet your needs?**

---

## 📊 Sample SMS Patterns Supported

### HDFC Bank
```
Rs.500 debited from A/C **1234 on 20-11-24 at SWIGGY BANGALORE. 
Avl Bal: Rs.15,000. Not you? Call 18002586161
```
**Extracts**: Amount=500, Wallet=1234, Merchant=SWIGGY, Category=Food & Dining

### ICICI Bank
```
Your A/c XX9876 is debited with INR 1,200.00 on 20NOV24 at 
ZOMATO DELHI. Available Balance: 25000.00
```
**Extracts**: Amount=1200, Wallet=9876, Merchant=ZOMATO, Category=Food & Dining

### Paytm
```
Rs.350 paid to UBER from Paytm Wallet. Txn ID: 123456789. 
Balance: Rs.5000
```
**Extracts**: Amount=350, Wallet=Paytm, Merchant=UBER, Category=Transportation

**Do these look correct?**

---

## 🎯 Success Metrics

After implementation, the app should:
- ✅ Classify 90%+ of bank transaction SMS correctly
- ✅ Auto-detect wallets from all major Indian banks
- ✅ Display dashboard charts with accurate data
- ✅ Allow wallet renaming with instant updates
- ✅ Scan specific SMS using debug filters
- ✅ Clear all data without crashes
- ✅ Build APK successfully via GitHub Actions

**Are these success criteria acceptable?**

---

## 🚀 Implementation Timeline

| Phase | Duration | Deliverables |
|-------|----------|--------------|
| **Phase 1**: Project Setup | 1 day | Project structure, dependencies, GitHub Actions |
| **Phase 2**: Core Logic | 2 days | SMS receiver, AI classifier, database |
| **Phase 3**: UI Development | 2 days | Dashboard, Wallets, Settings screens |
| **Phase 4**: Testing & Polish | 1 day | Bug fixes, UI polish, testing |

**Total Estimated Time**: 6 days

**Is this timeline acceptable?**

---

## 📦 Final Deliverables

1. ✅ Complete Android project (Kotlin + Compose)
2. ✅ GitHub Actions workflow (`.github/workflows/android-build.yml`)
3. ✅ README with setup instructions
4. ✅ Sample SMS data for testing
5. ✅ APK file (downloadable from GitHub Actions)

**Any additional deliverables needed?**

---

## ❓ Questions for You

### 1. Category Approval
**Question**: Are the 10 spending categories (Food, Transportation, Health, Shopping, Groceries, Utilities, Entertainment, Education, Travel, Miscellaneous) sufficient?

**Your Answer**: _______________________

### 2. Wallet Detection
**Question**: Should the app detect UPI IDs (like yourname@paytm) in addition to card numbers?

**Your Answer**: _______________________

### 3. Transaction Notifications
**Question**: Do you want notifications for every transaction? (e.g., "New transaction: Rs.500 at Swiggy")

**Your Answer**: _______________________

### 4. Dark Mode
**Question**: Should the app support dark mode by default?

**Your Answer**: _______________________

### 5. Export Data
**Question**: Do you want a feature to export transactions as CSV/Excel?

**Your Answer**: _______________________

---

## ✅ Final Approval

Once you review:
- ✅ Spending categories
- ✅ Technical stack
- ✅ Screen designs
- ✅ Testing features
- ✅ Timeline

**Type "APPROVED" or provide feedback on any changes needed.**

---

**Current Status**: ⏳ Awaiting Your Approval

**Next Step**: Begin Phase 1 (Project Setup) after approval

