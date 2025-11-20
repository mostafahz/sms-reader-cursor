package com.smsreader.financetracker.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallets",
    indices = [Index(value = ["wallet_id"], unique = true)]
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

