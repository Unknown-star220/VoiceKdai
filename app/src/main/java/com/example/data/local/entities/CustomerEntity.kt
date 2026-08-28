package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val businessId: String = "biz_default",
    val name: String,
    val phone: String = "",
    val currentBalance: Double = 0.0, // Positive = Customer owes merchant (Udhaar/You'll get), Negative = Merchant owes customer (Advance/You'll give)
    val lastTransactionAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
