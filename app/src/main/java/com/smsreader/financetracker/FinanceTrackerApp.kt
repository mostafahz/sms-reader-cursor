package com.smsreader.financetracker

import android.app.Application
import com.smsreader.financetracker.util.NotificationHelper

class FinanceTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
    }
}

