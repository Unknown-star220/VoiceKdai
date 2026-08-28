package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class BlueprintPart(
    val partNumber: Int,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val content: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemBlueprintScreen(
    activePart: Int,
    onSelectPart: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val blueprintParts = remember {
        listOf(
            BlueprintPart(
                partNumber = 1,
                title = "Part 1: Product Summary & Value Proposition",
                subtitle = "Tagline, Problem, Solution & Differentiators",
                icon = Icons.Default.Lightbulb,
                content = """
# VOICEKADAI — AI VOICE BUSINESS ASSISTANT

**Tagline:** "Speak your business. We handle the records."

### 1. Problem Statement
Indian small retail merchants (Kirana, hardware, textile, auto repair, vegetable vendors) lose 2 to 3 hours daily writing manual 'katha' notebooks. Manual registers lead to:
- ₹5,000–₹25,000 in forgotten customer udhaar (credit) per month.
- Delayed payment reminders causing cash-flow crunches.
- Clunky traditional apps requiring 8+ taps per sale (impractical during shop rush hours).

### 2. Core Solution
VoiceKadai eliminates typing. A merchant presses one button and speaks naturally in Tanglish, Tamil, Hindi, or Hinglish (*"Kumar kitta 5000 balance irukku"*, *"Ramesh 200 tea podi kudutharu"*). The AI extracts structured intent with Zero Financial Hallucination, while the database calculates balances deterministically.

### 3. Key Differentiators
- **Voice-First Simplicity:** 1-tap recording vs. 8-tap traditional software.
- **Dialect & Tanglish Support:** Native comprehension of regional business vernacular.
- **Action-Gate Invariant:** No financial transaction is committed without clear visual verification.
- **WhatsApp Integrated Reminders:** One-tap friendly billing notifications with local UPI QR links.
"""
            ),
            BlueprintPart(
                partNumber = 2,
                title = "Part 2: MVP Scope (Included vs. Excluded)",
                subtitle = "Strict Boundary Definitions for V1 Delivery",
                icon = Icons.Default.FactCheck,
                content = """
### MVP Scope Matrix

#### ✅ INCLUDED IN MVP:
1. **Voice-to-Ledger Engine:** Push-to-talk transcription + NLU for 4 core intents:
   - Udhaar Credit Sale (*"Kumar kitta 5000 balance"*)
   - Payment Received (*"Murugan 1000 kudutharu"*)
   - Business Expense (*"Current bill 1450 kattiyaachu"*)
   - Due Reminder (*"Priya 2200 naalaiki vasul"*)
2. **Deterministic Khata Ledger:** Customer balance tracking (+Udhaar / -Jama) with immutable audit history.
3. **Safety Action-Gate UI:** Visual modal displaying parsed name, amount, items, and quick +/- adjustment chips before database commit.
4. **WhatsApp Payment Reminders:** Dynamic localization with direct deep links.
5. **Conversational Business Analytics:** Deterministic SQL queries (*"Who owes me money?", "Today total sales?"*).
6. **Local-First SQLite / Room Persistence:** Offline operational resilience.

#### ❌ EXCLUDED FROM MVP (Future Roadmap):
- Full POS thermal printer Bluetooth hardware pairing.
- Automated GST / E-Way bill filing engine.
- Direct merchant bank account auto-debit integration.
- Multi-branch franchise inventory syncing.
"""
            ),
            BlueprintPart(
                partNumber = 3,
                title = "Part 3: Comprehensive User Flows",
                subtitle = "End-to-End Visual Lifecycles & State Transitions",
                icon = Icons.Default.AccountTree,
                content = """
### User Flow 1: Spoken Voice Entry to Ledger Commit
1. **Merchant Action:** Merchant taps Floating Microphone on Dashboard.
2. **Audio Capture:** Speech input is captured via Android Recognizer or AudioStream.
3. **Hybrid AI Parsing:**
   - Intent Classification (CREATE_SALE, RECORD_PAYMENT, EXPENSE, REMINDER, QUERY).
   - Entity Extraction (CustomerName, Amount, Currency, Items, DueDate).
4. **Action Gate Verification Modal:**
   - Displays parsed card: "Kumar (Hardware) | ₹5,000 | Udhaar (Gave ₹)".
   - Merchant verifies or edits with 1 tap.
5. **Database Transaction:** Relational update:
   `Customer.currentBalance = Customer.currentBalance + 5000`
   `Transaction.insert(type='SALE_CREDIT', amount=5000)`
6. **Confirmation:** Haptic feedback + Audio chime + Toast notification.

### User Flow 2: Automated Payment Reminder Lifecycle
1. **Trigger:** Due date threshold reached or Voice intent (*"Naalaiki vasul"*).
2. **Reminder Card Generation:** Displayed on Dashboard with countdown chip ("Due Tomorrow").
3. **WhatsApp Share:** Pre-populated friendly message in merchant's language.
4. **Settlement:** Merchant records payment via voice, auto-closing reminder.
"""
            ),
            BlueprintPart(
                partNumber = 4,
                title = "Part 4: Screen-by-Screen UX Specifications",
                subtitle = "Information Architecture & Interaction Design",
                icon = Icons.Default.PhoneAndroid,
                content = """
### Screen Layout Specifications

1. **Dashboard Screen:**
   - **Hero Cards:** Total Udhaar (You'll Get in Red), Today's Sales (in Emerald), Today's Expenses (in Amber).
   - **Hero Mic Floating Sheet:** Real-time waveform visualizer, push-to-talk button, and sample prompt chips.
   - **Quick Action Bar:** Sale (+), Payment Got (-), Expense (Bill), Reminder.
   - **Recent Activity Feed:** Color-coded transaction cards with timestamps and voice audio badges.

2. **Khata Directory (Customers Screen):**
   - Search bar + Status filters: All | Udhaar (Due) | Advance | Settled.
   - Customer Cards displaying avatar, name, phone, net balance, and last activity.
   - FAB to quickly add new customer with opening balance.

3. **Customer Ledger Detail Screen:**
   - Customer Header with phone, total balance, and WhatsApp Share button.
   - Dual bottom bar: 'Gave ₹ (Udhaar)' in Red and 'Got ₹ (Jama)' in Green.
   - Complete ledger chronological transaction history.

4. **Business Analytics AI Screen:**
   - Conversational prompt interface with pre-built merchant query chips.
   - Relational analytical breakdown cards with SQL transparency.
"""
            ),
            BlueprintPart(
                partNumber = 5,
                title = "Part 5: Technical Stack Architecture",
                subtitle = "Mobile, Backend, Database, AI & Cache Specification",
                icon = Icons.Default.Layers,
                content = """
### Enterprise System Stack

- **Mobile Client:** Android Native with Kotlin & Jetpack Compose (MVVM Architecture) / Flutter 3.x.
- **Local Persistence:** Room Database with SQLite for zero-latency offline performance.
- **Backend API Gateway:** Node.js Fastify or Python FastAPI with async connection pooling.
- **Primary Relational Database:** PostgreSQL 16 managed via Prisma ORM / Cloud SQL.
- **Cache & Async Queue:** Redis 7 + BullMQ for asynchronous voice audio transcription & reminder dispatch.
- **AI / LLM Orchestration:** Gemini 2.5/3.5 Flash via Server-Side REST API with strict Structured JSON Output (`responseMimeType: application/json`).
- **Security & Secrets:** Google AI Studio Secrets Panel + Android `BuildConfig` injection.
"""
            ),
            BlueprintPart(
                partNumber = 6,
                title = "Part 6: Database Schema & Relational Design",
                subtitle = "Complete Prisma / Room Entities & Index Strategies",
                icon = Icons.Default.Storage,
                content = """
### Relational Schema Specification (Prisma / SQL)

```prisma
enum PlanTier { FREE, PRO, BUSINESS }
enum TxType { SALE_CASH, SALE_CREDIT, PAYMENT_RECEIVED }
enum ExpenseCategory { STOCK, RENT, ELECTRICITY, TRANSPORT, CHAI_SNACKS, STAFF_SALARY, OTHER }
enum ReminderStatus { PENDING, SENT, SETTLED }

model Business {
  id                  String        @id @default(uuid())
  name                String
  ownerName           String
  phone               String        @unique
  category            String
  planTier            PlanTier      @default(FREE)
  dailyVoiceCount     Int           @default(0)
  createdAt           DateTime      @default(now())
  customers           Customer[]
  transactions        Transaction[]
  expenses            Expense[]
  reminders           Reminder[]
}

model Customer {
  id                  String        @id @default(uuid())
  businessId          String
  name                String
  phone               String
  currentBalance      Decimal       @default(0.0) // (+) Udhaar (Gave), (-) Advance
  lastTransactionAt   DateTime      @default(now())
  business            Business      @relation(fields: [businessId], references: [id])
  transactions        Transaction[]
  reminders           Reminder[]
  @@index([businessId, name])
}

model Transaction {
  id                  String        @id @default(uuid())
  businessId          String
  customerId          String?
  type                TxType
  amount              Decimal
  paymentMode         String        @default("CASH") // CASH, UPI, CREDIT
  audioTranscript     String?
  note                String?
  createdAt           DateTime      @default(now())
  business            Business      @relation(fields: [businessId], references: [id])
  customer            Customer?     @relation(fields: [customerId], references: [id])
  @@index([businessId, createdAt])
}
```
"""
            ),
            BlueprintPart(
                partNumber = 7,
                title = "Part 7: AI System & Structured Prompts",
                subtitle = "NLU Pipeline, Zero-Hallucination Gate & Few-Shot Templates",
                icon = Icons.Default.Psychology,
                content = """
### AI Orchestration & Structured Prompt Invariant

```json
{
  "system_instruction": "You are VoiceKadai NLU. Parse Indian merchant voice inputs in English, Tamil, Hindi, Tanglish and Hinglish into strict JSON. Follow Zero Financial Hallucination invariant.",
  "generation_config": {
    "temperature": 0.1,
    "response_mime_type": "application/json"
  },
  "schema": {
    "intent": "CREATE_SALE | RECORD_PAYMENT | CREATE_EXPENSE | CREATE_REMINDER | QUERY_BUSINESS_DATA",
    "customerName": "string | null",
    "amount": "number",
    "isCredit": "boolean",
    "paymentMode": "CASH | UPI | CREDIT",
    "expenseCategory": "string | null",
    "items": [{"name": "string", "quantity": "number", "price": "number"}],
    "summaryEnglish": "string",
    "summaryRegional": "string"
  }
}
```

#### Verification Principle:
1. **Entity Extraction Only:** The AI model is strictly restricted to semantic classification.
2. **Zero In-Model Arithmetic:** The database computes all balances deterministically.
3. **Action-Gate Enforcement:** No mutation is applied until the user confirms or adjusts the parsed payload.
"""
            ),
            BlueprintPart(
                partNumber = 8,
                title = "Part 8: Security, Data Isolation & Privacy",
                subtitle = "Multi-Tenant Isolation, Idempotency & RBAC",
                icon = Icons.Default.Shield,
                content = """
### Security & Compliance Safeguards

1. **Strict Multi-Tenant Isolation:** Every database query enforces `WHERE business_id = :authenticated_business_id`.
2. **Idempotency Keys:** Every voice transaction submission includes a unique `idempotency_key = UUID(audio_hash + timestamp)` preventing accidental double ledger entries during network jitter.
3. **Audio Privacy & Ephemeral Processing:** Raw voice audio buffers are discarded immediately after transcription; only the text transcript is stored for merchant audit logs.
4. **Data Encryption:** TLS 1.3 in transit and AES-256 at rest for all merchant ledger databases.
"""
            ),
            BlueprintPart(
                partNumber = 9,
                title = "Part 9: Monetization & Unit Economics",
                subtitle = "Pricing Tiers, Gross Margins & Scale Economics",
                icon = Icons.Default.MonetizationOn,
                content = """
### Pricing Tiers & Unit Economics

| Tier | Price | Voice Quota | Key Features |
|---|---|---|---|
| **Free (Kadaikaran)** | ₹0 / mo | 15 voice entries/day | Basic Khata ledger, Single device |
| **Pro (Vyapar)** | ₹299 / mo | 100 voice entries/day | Unlimited AI Analytics, WhatsApp Reminders |
| **Business (Super)** | ₹999 / mo | Unlimited entries | Multi-staff logins, Automated Audio Reminders |

#### Unit Economics at Scale:
- **Average Gemini 2.5/3.5 Flash Token Cost:** ~₹0.004 per voice parsing call.
- **Monthly AI Cost per Active Pro User (75 calls/day):** ~₹9.00 / month.
- **Server & Database Infrastructure Cost per User:** ~₹15.00 / month.
- **Gross Profit Margin on ₹299/mo Pro Plan:** **~91.9% Gross Margin**.
"""
            ),
            BlueprintPart(
                partNumber = 10,
                title = "Part 10: Development Roadmap & Timeline",
                subtitle = "8-Week Execution Plan from Sprint 0 to Production",
                icon = Icons.Default.CalendarMonth,
                content = """
### 8-Week Implementation Roadmap

- **Week 1-2 (Foundation & DB):**
  - Room / PostgreSQL Schema setup.
  - Multi-tenant business authentication & base UI scaffolding.
- **Week 3-4 (Voice & NLU Engine):**
  - Android SpeechRecognizer + Gemini REST API pipeline.
  - Action-Gate verification modal with Zero-Hallucination guarantees.
- **Week 5-6 (Khata Ledger & Reminders):**
  - Customer directory, ledger transaction histories, Udhaar/Jama calculators.
  - WhatsApp reminder share intent generators.
- **Week 7 (Conversational AI Analytics):**
  - Natural language query answering engine over local relational database.
- **Week 8 (Testing & Pilot Launch):**
  - Beta testing across 50 Kirana & Hardware shops in Chennai/Coimbatore.
  - Production deployment & Play Store launch.
"""
            ),
            BlueprintPart(
                partNumber = 11,
                title = "Part 11: Technical & Business Risk Matrix",
                subtitle = "Failure Modes, Risk Severity & Proactive Mitigations",
                icon = Icons.Default.WarningAmber,
                content = """
### Risk & Mitigation Matrix

| Risk Factor | Severity | Mitigation Strategy |
|---|---|---|
| **Noisy Shop Environment** | HIGH | Implement local deterministic regex keyword parser fallback for background noise. |
| **Merchant Distrust of AI Math** | CRITICAL | Strictly enforce database arithmetic + prominent Action-Gate visual confirmation modal. |
| **Offline Rural Connectivity** | HIGH | Local-first Room SQLite storage with async sync queue when online. |
| **Regional Accent Variances** | MEDIUM | Support mixed Tanglish, Hinglish, and colloquial Tamil vocabulary in prompt few-shots. |
"""
            ),
            BlueprintPart(
                partNumber = 12,
                title = "Part 12: Immediate Action Plan: First 7 Days",
                subtitle = "Sprint Backlog & Launch Deliverables",
                icon = Icons.Default.RocketLaunch,
                content = """
### First 7-Day Sprint Action Plan

- **Day 1:** Finalize Room entities (`Business`, `Customer`, `Transaction`, `Expense`, `Reminder`).
- **Day 2:** Build Action-Gate Verification Modal with quick-edit amount chips.
- **Day 3:** Integrate Gemini Flash API with structured JSON output and fallback local engine.
- **Day 4:** Implement Push-to-Talk waveform audio UI and Tanglish simulation chips.
- **Day 5:** Build Customer Khata Ledger, Udhaar/Jama calculators, and WhatsApp share flow.
- **Day 6:** Build Conversational AI Query engine for real-time shop analytics.
- **Day 7:** Conduct end-to-end verification, verify compile_applet build, and deliver production release!
"""
            )
        )
    }

    val selectedPartObj = blueprintParts.find { it.partNumber == activePart } ?: blueprintParts.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("System Architecture Blueprint", fontWeight = FontWeight.Bold)
                        Text("12-Part Production Specification", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        color = EmeraldContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Part $activePart of 12",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnEmeraldContainer
                        )
                    }
                }
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .testTag("system_blueprint_screen")
        ) {
            // Horizontal Part Tabs Selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(blueprintParts) { part ->
                    FilterChip(
                        selected = part.partNumber == activePart,
                        onClick = { onSelectPart(part.partNumber) },
                        leadingIcon = {
                            Icon(imageVector = part.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = {
                            Text("Part ${part.partNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Part Content Body
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = EmeraldContainer,
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = selectedPartObj.icon, contentDescription = null, tint = OnEmeraldContainer, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = selectedPartObj.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = selectedPartObj.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = selectedPartObj.content.trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Next Part Navigation Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (activePart > 1) {
                            OutlinedButton(
                                onClick = { onSelectPart(activePart - 1) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Part ${activePart - 1}")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (activePart < 12) {
                            Button(
                                onClick = { onSelectPart(activePart + 1) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Next: Part ${activePart + 1}")
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
