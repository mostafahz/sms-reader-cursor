package com.smsreader.financetracker.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

    init {
        currencyFormat.currency = Currency.getInstance("INR")
    }

    fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }

    fun formatAmount(amount: Double): String {
        return "₹%.2f".format(amount)
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun formatCompactNumber(number: Double): String {
        return when {
            number >= 10000000 -> "₹%.2f Cr".format(number / 10000000)
            number >= 100000 -> "₹%.2f L".format(number / 100000)
            number >= 1000 -> "₹%.2f K".format(number / 1000)
            else -> "₹%.0f".format(number)
        }
    }
}

