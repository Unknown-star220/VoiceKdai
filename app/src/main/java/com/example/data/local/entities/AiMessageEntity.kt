package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey val id: String,
    val businessId: String = "biz_default",
    val role: String, // "user", "assistant"
    val content: String,
    val intentType: String? = null,
    val rawJsonPayload: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
