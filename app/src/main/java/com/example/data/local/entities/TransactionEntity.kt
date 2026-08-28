package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val businessId: String = "biz_default",
    val customerId: String? = null,
    val customerName: String? = null,
    val type: String, // SALE_CASH, SALE_CREDIT (Udhaar), PAYMENT_RECEIVED (Jama), PAYMENT_GIVEN
    val amount: Double,
    val paymentMode: String = "CASH", // CASH, UPI, CREDIT
    val note: String = "",
    val itemsJson: String = "", // e.g. [{"name":"Rice 25kg","price":1400,"qty":1}]
    val audioTranscript: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
