package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "subscription_payments")
data class SubscriptionPaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String = "biz_default",
    val orderId: String,
    val planTier: String, // "PRO", "BUSINESS"
    val billingCycle: String, // "MONTHLY", "ANNUAL"
    val amount: Double,
    val taxAmount: Double, // 18% GST in India
    val totalPaid: Double,
    val paymentMethod: String, // "GOOGLE_PLAY", "UPI_GPAY", "UPI_PHONEPE", "RAZORPAY_CARDS"
    val transactionRef: String,
    val status: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    val invoiceNumber: String,
    val createdAt: Long = System.currentTimeMillis()
)
