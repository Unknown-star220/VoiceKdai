package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val businessId: String = "biz_default",
    val category: String, // STOCK, RENT, ELECTRICITY, TRANSPORT, CHAI_SNACKS, STAFF_SALARY, MAINTENANCE, OTHER
    val amount: Double,
    val note: String = "",
    val paymentMode: String = "CASH",
    val createdAt: Long = System.currentTimeMillis()
)
