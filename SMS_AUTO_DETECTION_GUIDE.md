# SMS Auto-Detection & Push Notifications Guide

## What's Been Fixed

### 1. Duplicate Detection
- Added `transactionExists()` method in `TransactionDao` to check for duplicate transactions
- Both `SmsReceiver` (real-time) and `SmsScanner` (manual scan) now check for duplicates before inserting
- Duplicates are identified by: `senderId`, `smsBody`, and `timestamp`

### 2. Enhanced SMS Receiver
- Improved error handling and logging in `SmsReceiver.kt`
- Added support for both old and new PDU formats (Android compatibility)
- Better debugging logs to track SMS reception

### 3. Push Notifications
- Already implemented via `NotificationHelper`
- Notifications are sent automatically when a financial transaction is detected
- Shows: Amount, Merchant, and Category
- Tapping notification opens the app

## How It Works

### Automatic SMS Detection Flow:
1. **New SMS Arrives** → Android broadcasts `SMS_RECEIVED` intent
2. **SmsReceiver catches it** → Extracts sender, body, timestamp
3. **Classification** → Checks if it's a banking SMS and classifies it
4. **Duplicate Check** → Verifies transaction doesn't already exist
5. **Save to DB** → Stores transaction and wallet info
6. **Push Notification** → Shows notification to user

### Key Features:
- ✅ Only processes DEBIT transactions (spending tracking)
- ✅ Prevents duplicate entries
- ✅ Creates/updates wallet information
- ✅ Shows push notifications with transaction details
- ✅ Works in background even when app is closed

## Testing Guide

### Prerequisites:
1. Install the updated app on your Android device
2. Grant all permissions:
   - READ_SMS
   - RECEIVE_SMS
   - POST_NOTIFICATIONS (Android 13+)

### Test Scenario 1: First Time Setup
1. Open the app and grant all permissions
2. Go to Settings → SMS Scanner
3. Click "Scan Now" to import existing SMS
4. Verify transactions appear in Dashboard

### Test Scenario 2: Receive New SMS
1. Have someone send you a test banking SMS OR
2. Use your banking app to make a small transaction
3. Wait for the SMS to arrive
4. Check for:
   - ✅ Push notification appears
   - ✅ Transaction shows in Dashboard
   - ✅ No duplicate entry if you scan again

### Test Scenario 3: Duplicate Prevention
1. Scan existing SMS messages
2. Receive a new SMS for the same transaction
3. Verify only ONE entry appears in the database

### Expected Notification Format:
```
Title: New Transaction
Body: ₹123.45 at Merchant Name (Category)
```

## Debugging

### If SMS auto-detection doesn't work:

1. **Check Logcat for these tags:**
   ```bash
   adb logcat -s SmsReceiver:D
   ```

2. **Look for these log messages:**
   - `onReceive called with action: android.provider.Telephony.SMS_RECEIVED`
   - `Received SMS from [sender]: [preview]...`
   - `Transaction saved: ID=...`
   - `Transaction already exists, skipping duplicate`

3. **Common Issues:**
   - **No logs at all** → SMS receiver not registered or permission not granted
   - **"Not a banking SMS"** → SMS doesn't match banking patterns
   - **"Could not classify SMS"** → Classification failed (amount/merchant not detected)
   - **"Credit transaction, skipping"** → Only tracking debits by design
   - **"Transaction already exists"** → Duplicate prevention working correctly

4. **Verify Permissions:**
   - Go to Settings → App Info → Permissions
   - Ensure READ_SMS, RECEIVE_SMS, and NOTIFICATIONS are granted

5. **Test with Known Banking SMS:**
   ```
   Example SMS formats that should work:
   - "Rs 150.00 debited from A/c XX1234 on 20-11-24 at Swiggy"
   - "Your A/c XX5678 debited with Rs.299.00 on 20-NOV-24 for Amazon"
   - "INR 1200.50 debited from Card XX9999 at Zomato on 20/11/24"
   ```

### If Notifications don't show:

1. **Check notification permission (Android 13+):**
   ```kotlin
   Settings → Apps → SMS Finance Tracker → Notifications → Enabled
   ```

2. **Check notification channel:**
   - Go to app notification settings
   - Ensure "Transaction Alerts" channel is enabled

3. **Verify in Logcat:**
   ```bash
   adb logcat -s NotificationHelper:D
   ```

## Code Changes Summary

### Modified Files:
1. **TransactionDao.kt**
   - Added `transactionExists()` method
   - Added `findDuplicateTransaction()` method

2. **SmsReceiver.kt**
   - Enhanced error handling and logging
   - Added duplicate check before insertion
   - Improved PDU parsing for Android compatibility

3. **SmsScanner.kt**
   - Added duplicate check before insertion during manual scans

## Permissions Required

```xml
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Architecture

```
SMS Arrives
    ↓
SmsReceiver (BroadcastReceiver)
    ↓
SmsClassifier (validates & classifies)
    ↓
TransactionDao (duplicate check)
    ↓
Database (insert transaction)
    ↓
NotificationHelper (show notification)
```

## Support

If you encounter any issues:
1. Check the logcat output (instructions above)
2. Verify all permissions are granted
3. Test with a known banking SMS format
4. Ensure your device isn't blocking background receivers

## Future Enhancements

Potential improvements:
- [ ] Add settings to enable/disable notifications
- [ ] Customize notification sound/vibration
- [ ] Add notification actions (e.g., "Mark as incorrect")
- [ ] Support for CREDIT transactions (income tracking)
- [ ] Machine learning for better merchant detection

