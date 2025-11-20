package com.smsreader.financetracker.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["wallet_id"]),
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

