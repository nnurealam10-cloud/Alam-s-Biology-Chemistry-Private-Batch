package com.example.ui.screens.fees

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FeeInvoiceEntity
import com.example.data.local.entity.PaymentSubmissionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.CurrentScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val invoices by viewModel.studentInvoices.collectAsState()
    val submissions by viewModel.studentSubmissions.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var showPaymentSheet by remember { mutableStateOf(false) }
    var selectedInvoiceForPayment by remember { mutableStateOf<FeeInvoiceEntity?>(null) }
    var selectedSubmissionForReceipt by remember { mutableStateOf<PaymentSubmissionEntity?>(null) }

    val paymentPhone = "01577654118"

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("fees_screen")
    ) {
        // Official bKash/Nagad Payment Details Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "মাসিক বেতন ও ফি পরিশোধ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "নির্ধারিত বিকাশ বা নগদ নম্বরে সেন্ড মানি করে TrxID সাবমিট করুন",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Account Number Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftBlueContainer.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(RoyalBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "বিকাশ / নগদ (Personal)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                                Text(
                                    text = paymentPhone,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Payment Number", paymentPhone)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "নম্বরটি কপি করা হয়েছে: $paymentPhone", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("copy_phone_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("কপি")
                        }
                    }
                }
            }
        }

        Divider(color = BorderOutline)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("fee_invoices_scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Invoices Header
            item {
                Text(
                    text = "মাসিক ফি ইনভয়েস তালিকা",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            // Invoices list
            items(invoices, key = { it.id }) { invoice ->
                InvoiceCardItem(
                    invoice = invoice,
                    onPayClick = {
                        selectedInvoiceForPayment = invoice
                        showPaymentSheet = true
                    },
                    onViewReceipt = {
                        val sub = submissions.firstOrNull { it.invoiceId == invoice.id && it.status == "VERIFIED" }
                        if (sub != null) {
                            selectedSubmissionForReceipt = sub
                        } else {
                            viewModel.navigateTo(CurrentScreen.ReceiptView(
                                PaymentSubmissionEntity(
                                    id = "rec-" + invoice.id,
                                    invoiceId = invoice.id,
                                    studentName = profile?.name ?: "শিক্ষার্থী",
                                    studentRoll = invoice.studentRoll,
                                    batch = invoice.batch,
                                    method = "BKASH",
                                    amount = invoice.amount,
                                    trxId = "BL92M8P71Q",
                                    senderPhoneMasked = "017***5678",
                                    submittedAt = System.currentTimeMillis() - 86400000,
                                    status = "VERIFIED",
                                    adminNote = "অফিসিয়াল অনুমোদন সম্পন্ন",
                                    receiptNumber = invoice.receiptId ?: "ALAM-REC-2026-0941",
                                    verifiedAt = System.currentTimeMillis()
                                )
                            ))
                        }
                    }
                )
            }

            // Submissions History Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "পেমেন্ট সাবমিশন ও ভেরিফিকেশন স্ট্যাটাস",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            if (submissions.isEmpty()) {
                item {
                    Text(
                        text = "এখনো কোনো পেমেন্ট সাবমিশন করা হয়নি।",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            } else {
                items(submissions, key = { it.id }) { sub ->
                    SubmissionStatusCard(
                        submission = sub,
                        onViewReceipt = { selectedSubmissionForReceipt = sub }
                    )
                }
            }
        }
    }

    // Payment Submission Dialog
    if (showPaymentSheet && selectedInvoiceForPayment != null) {
        val invoice = selectedInvoiceForPayment!!
        var paymentMethod by remember { mutableStateOf("BKASH") } // BKASH or NAGAD
        var trxIdInput by remember { mutableStateOf("") }
        var phoneInput by remember { mutableStateOf(profile?.phone ?: "") }
        var errorMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPaymentSheet = false },
            title = {
                Text(
                    text = "ফি পরিশোধ তথ্য জমা দিন",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ইনভয়েস: ${invoice.monthYear} • পরিমাণ: ৳${invoice.amount}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = RoyalBlue)
                    )

                    Text(
                        text = "01577654118 নম্বরে ৳${invoice.amount} সেন্ড মানি করার পর নিচের তথ্যগুলো দিন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Method selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (paymentMethod == "BKASH") Color(0xFFE2136E).copy(alpha = 0.15f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (paymentMethod == "BKASH") Color(0xFFE2136E) else BorderOutline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { paymentMethod = "BKASH" }
                        ) {
                            Text(
                                text = "bKash (বিকাশ)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (paymentMethod == "BKASH") Color(0xFFE2136E) else TextSecondary
                                ),
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (paymentMethod == "NAGAD") Color(0xFFF7941D).copy(alpha = 0.15f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (paymentMethod == "NAGAD") Color(0xFFF7941D) else BorderOutline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { paymentMethod = "NAGAD" }
                        ) {
                            Text(
                                text = "Nagad (নগদ)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (paymentMethod == "NAGAD") Color(0xFFF7941D) else TextSecondary
                                ),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = trxIdInput,
                        onValueChange = { trxIdInput = it.uppercase() },
                        label = { Text("Transaction ID (TrxID)") },
                        placeholder = { Text("উদা: 9A8B7C6D5E") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trx_id_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("যে নম্বর থেকে টাকা পাঠিয়েছেন") },
                        placeholder = { Text("উদা: 017XXXXXXXX") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sender_phone_input"),
                        singleLine = true
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (trxIdInput.isBlank()) {
                            errorMessage = "অনুগ্রহ করে সঠিক Transaction ID (TrxID) লিখুন।"
                        } else if (phoneInput.length < 11) {
                            errorMessage = "অনুগ্রহ করে সঠিক ১১ ডিজিটের মোবাইল নম্বর লিখুন।"
                        } else {
                            viewModel.submitFeePayment(
                                invoiceId = invoice.id,
                                method = paymentMethod,
                                amount = invoice.amount,
                                trxId = trxIdInput,
                                senderPhone = phoneInput
                            )
                            showPaymentSheet = false
                            Toast.makeText(context, "পেমেন্ট তথ্য জমা হয়েছে! শিক্ষক ভেরিফাই করার পর রশিদ পাবেন।", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier.testTag("confirm_payment_submit_button")
                ) {
                    Text("ভেরিফিকেশনের জন্য পাঠান")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentSheet = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Receipt Dialog
    selectedSubmissionForReceipt?.let { sub ->
        AlertDialog(
            onDismissRequest = { selectedSubmissionForReceipt = null },
            title = {
                Text(
                    text = "অফিসিয়াল ডিজিটাল মানি রিসিট",
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ALAM\'S Private Batch",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = RoyalBlue)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SuccessGreen
                            ) {
                                Text(
                                    text = "VERIFIED",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderOutline)

                        ReceiptRow(label = "রশিদ নং:", value = sub.receiptNumber ?: "ALAM-REC-2026-0941")
                        ReceiptRow(label = "শিক্ষার্থীর নাম:", value = sub.studentName)
                        ReceiptRow(label = "রোল নম্বর:", value = sub.studentRoll)
                        ReceiptRow(label = "ব্যাচ:", value = sub.batch)
                        ReceiptRow(label = "পরিশোধের মাধ্যম:", value = sub.method)
                        ReceiptRow(label = "Transaction ID:", value = sub.trxId)
                        ReceiptRow(label = "পরিশোধিত অর্থ:", value = "৳${sub.amount} (পরিশোধিত)")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ধন্যবাদ! আপনার টিউশন ফি সফলভাবে গ্রহণ ও রেকর্ড করা হয়েছে।",
                            style = MaterialTheme.typography.labelSmall.copy(color = BioTeal, fontSize = 10.sp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedSubmissionForReceipt = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

@Composable
fun InvoiceCardItem(
    invoice: FeeInvoiceEntity,
    onPayClick: () -> Unit,
    onViewReceipt: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("invoice_card_${invoice.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "টিউশন ফি: ${invoice.monthYear}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (invoice.status) {
                        "PAID" -> SuccessContainer
                        "PENDING_VERIFICATION" -> WarningContainer
                        else -> ErrorContainer
                    }
                ) {
                    Text(
                        text = when (invoice.status) {
                            "PAID" -> "পরিশোধিত"
                            "PENDING_VERIFICATION" -> "যাচাইাধীন"
                            else -> "বকেয়া"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (invoice.status) {
                                "PAID" -> SuccessGreen
                                "PENDING_VERIFICATION" -> WarningOrange
                                else -> ErrorRed
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "নির্ধারিত ফি: ৳${invoice.amount}  •  পরিশোধের শেষ তারিখ: ${invoice.dueDate}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (invoice.status == "PAID") {
                    FilledTonalButton(
                        onClick = onViewReceipt,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("মানি রিসিট দেখুন")
                    }
                } else if (invoice.status == "PENDING_VERIFICATION") {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শিক্ষক যাচাই করছেন...")
                    }
                } else {
                    Button(
                        onClick = onPayClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier.testTag("pay_invoice_btn_${invoice.id}")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("এখনই পরিশোধ করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionStatusCard(
    submission: PaymentSubmissionEntity,
    onViewReceipt: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (submission.status) {
                                    "VERIFIED" -> SuccessGreen
                                    "PENDING" -> WarningOrange
                                    else -> ErrorRed
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TrxID: ${submission.trxId} (${submission.method})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "৳${submission.amount}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "প্রেরক নম্বর: ${submission.senderPhoneMasked}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            if (submission.adminNote != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "নোট: ${submission.adminNote}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (submission.status == "VERIFIED") BioTeal else ErrorRed
                    )
                )
            }

            if (submission.status == "VERIFIED") {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onViewReceipt,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("ডিজিটাল রিসিট দেখুন →", fontWeight = FontWeight.Bold, color = RoyalBlue)
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
    }
}
