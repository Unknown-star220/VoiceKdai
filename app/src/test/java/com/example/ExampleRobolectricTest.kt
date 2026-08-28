package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.IntentType
import com.example.data.remote.GeminiService
import com.example.data.repository.VoiceKadaiRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VoiceKadai", appName)
    }

    @Test
    fun `test Tanglish Udhaar parsing`() {
        val payload = GeminiService.parseLocally("Kumar kitta 5000 balance irukku")
        assertEquals(IntentType.CREATE_SALE, payload.intent)
        assertEquals("Kumar", payload.customerName)
        assertEquals(5000.0, payload.amount, 0.01)
        assertTrue(payload.isCredit)
    }

    @Test
    fun `test Tanglish Jama payment received parsing`() {
        val payload = GeminiService.parseLocally("Murugan 1000 kudutharu")
        assertEquals(IntentType.RECORD_PAYMENT, payload.intent)
        assertEquals("Murugan", payload.customerName)
        assertEquals(1000.0, payload.amount, 0.01)
        assertEquals(false, payload.isCredit)
    }

    @Test
    fun `test Tanglish Expense parsing`() {
        val payload = GeminiService.parseLocally("Current bill 1450 kattiyaachu")
        assertEquals(IntentType.CREATE_EXPENSE, payload.intent)
        assertEquals("ELECTRICITY", payload.expenseCategory)
        assertEquals(1450.0, payload.amount, 0.01)
    }

    @Test
    fun `test Tanglish Reminder parsing`() {
        val payload = GeminiService.parseLocally("Deepak kitta 1500 vasul pannanum naalaiki")
        assertEquals(IntentType.CREATE_REMINDER, payload.intent)
        assertEquals("Deepak", payload.customerName)
        assertEquals(1500.0, payload.amount, 0.01)
    }

    @Test
    fun `test developer email starts with PRO plan and regular email starts with FREE plan`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val repository = VoiceKadaiRepository(db.voiceKadaiDao())

        // 1. Developer Login
        repository.signInWithProvider(
            provider = "GOOGLE",
            email = "Safiya.umar13@gmail.com",
            displayName = "Safiya Umar",
            businessName = "Umar Kirana"
        )
        val devBiz = db.voiceKadaiDao().getBusiness()
        assertNotNull(devBiz)
        assertEquals("PRO", devBiz?.planTier)
        assertEquals(100, devBiz?.maxDailyVoiceQuota)

        // 2. Regular Merchant Login
        repository.signInWithProvider(
            provider = "GOOGLE",
            email = "another.merchant@gmail.com",
            displayName = "Ramesh Store",
            businessName = "Ramesh General Store"
        )
        val regularBiz = db.voiceKadaiDao().getBusiness()
        assertNotNull(regularBiz)
        assertEquals("FREE", regularBiz?.planTier)
        assertEquals(5, regularBiz?.maxDailyVoiceQuota)
    }

    @Test
    fun `test subscription payment calculation and persistence`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val repository = VoiceKadaiRepository(db.voiceKadaiDao())

        val payment = repository.processSubscriptionPayment(
            planTier = "PRO",
            billingCycle = "MONTHLY",
            paymentMethod = "UPI_GPAY",
            transactionRef = "UPI_TEST_123"
        )

        assertEquals("PRO", payment.planTier)
        assertEquals(299.0, payment.amount, 0.01)
        assertEquals(53.82, payment.taxAmount, 0.01)
        assertEquals(352.82, payment.totalPaid, 0.01)
        assertNotNull(payment.orderId)
        assertNotNull(payment.invoiceNumber)
    }
}
