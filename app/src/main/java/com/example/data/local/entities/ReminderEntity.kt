package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val businessId: String = "biz_default",
    val customerId: String,
    val customerName: String,
    val amount: Double,
    val dueDate: Long,
    val status: String = "PENDING", // PENDING, SENT, SETTLED
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
