package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceKadaiDao {
    // --- Business ---
    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    fun getBusinessFlow(id: String = "biz_default"): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    suspend fun getBusiness(id: String = "biz_default"): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    // --- Customers ---
    @Query("SELECT * FROM customers WHERE businessId = :bizId ORDER BY lastTransactionAt DESC")
    fun getAllCustomers(bizId: String = "biz_default"): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE businessId = :bizId AND id = :customerId LIMIT 1")
    fun getCustomerByIdFlow(customerId: String, bizId: String = "biz_default"): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE businessId = :bizId AND id = :customerId LIMIT 1")
    suspend fun getCustomerById(customerId: String, bizId: String = "biz_default"): CustomerEntity?

    @Query("SELECT * FROM customers WHERE businessId = :bizId AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findCustomerByName(name: String, bizId: String = "biz_default"): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomer(customerId: String)

    @Query("SELECT COALESCE(SUM(CASE WHEN currentBalance > 0 THEN currentBalance ELSE 0 END), 0.0) FROM customers WHERE businessId = :bizId")
    fun getTotalUdhaarToReceive(bizId: String = "biz_default"): Flow<Double>

    @Query("SELECT COALESCE(SUM(CASE WHEN currentBalance < 0 THEN ABS(currentBalance) ELSE 0 END), 0.0) FROM customers WHERE businessId = :bizId")
    fun getTotalAdvanceToGive(bizId: String = "biz_default"): Flow<Double>

    // --- Transactions ---
    @Query("SELECT * FROM transactions WHERE businessId = :bizId ORDER BY createdAt DESC")
    fun getAllTransactions(bizId: String = "biz_default"): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE businessId = :bizId AND customerId = :customerId ORDER BY createdAt DESC")
    fun getTransactionsForCustomer(customerId: String, bizId: String = "biz_default"): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE businessId = :bizId AND (type = 'SALE_CASH' OR type = 'SALE_CREDIT') AND createdAt >= :startOfDayTimestamp")
    fun getTodaySales(startOfDayTimestamp: Long, bizId: String = "biz_default"): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE businessId = :bizId AND type = 'PAYMENT_RECEIVED' AND createdAt >= :startOfDayTimestamp")
    fun getTodayCashInReceived(startOfDayTimestamp: Long, bizId: String = "biz_default"): Flow<Double>

    // --- Expenses ---
    @Query("SELECT * FROM expenses WHERE businessId = :bizId ORDER BY createdAt DESC")
    fun getAllExpenses(bizId: String = "biz_default"): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE businessId = :bizId AND createdAt >= :startOfDayTimestamp")
    fun getTodayExpenses(startOfDayTimestamp: Long, bizId: String = "biz_default"): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE businessId = :bizId")
    fun getTotalExpenses(bizId: String = "biz_default"): Flow<Double>

    // --- Reminders ---
    @Query("SELECT * FROM reminders WHERE businessId = :bizId ORDER BY dueDate ASC")
    fun getAllReminders(bizId: String = "biz_default"): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE businessId = :bizId AND status = 'PENDING' ORDER BY dueDate ASC")
    fun getPendingReminders(bizId: String = "biz_default"): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateReminderStatus(id: String, status: String)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)

    // --- AI Messages ---
    @Query("SELECT * FROM ai_messages WHERE businessId = :bizId ORDER BY timestamp ASC")
    fun getAllAiMessages(bizId: String = "biz_default"): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessage(message: AiMessageEntity)

    @Query("DELETE FROM ai_messages WHERE businessId = :bizId")
    suspend fun clearAiMessages(bizId: String = "biz_default")

    // --- Subscriptions & Payments ---
    @Query("SELECT * FROM subscription_payments WHERE businessId = :bizId ORDER BY createdAt DESC")
    fun getAllPayments(bizId: String = "biz_default"): Flow<List<SubscriptionPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: SubscriptionPaymentEntity)
}

