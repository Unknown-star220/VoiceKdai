package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey val id: String = "biz_default",
    val name: String = "My Kirana Store",
    val ownerName: String = "Merchant",
    val category: String = "Kirana & Provision",
    val phone: String = "",
    val language: String = "Tanglish",
    val currencySymbol: String = "₹",
    val planTier: String = "FREE", // FREE, PRO, BUSINESS
    val dailyVoiceCount: Int = 0,
    val maxDailyVoiceQuota: Int = 5,
    val isSignedIn: Boolean = false,
    val authProvider: String = "GOOGLE", // "GOOGLE", "MICROSOFT", "PHONE", "GUEST"
    val userEmail: String = "",
    val userDisplayName: String = "Merchant",
    val userPhotoUrl: String? = null,
    val subscriptionExpiry: Long = 0L,
    val lastPaymentOrderId: String? = null,
    val lastPaymentAmount: Double = 0.0,
    val billingCycle: String = "MONTHLY", // "MONTHLY", "ANNUAL"
    val createdAt: Long = System.currentTimeMillis()
)
