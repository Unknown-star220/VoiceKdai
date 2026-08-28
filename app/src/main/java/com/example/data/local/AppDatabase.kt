package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.VoiceKadaiDao
import com.example.data.local.entities.*

@Database(
    entities = [
        BusinessEntity::class,
        CustomerEntity::class,
        TransactionEntity::class,
        ExpenseEntity::class,
        ReminderEntity::class,
        AiMessageEntity::class,
        SubscriptionPaymentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voiceKadaiDao(): VoiceKadaiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voicekadai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
