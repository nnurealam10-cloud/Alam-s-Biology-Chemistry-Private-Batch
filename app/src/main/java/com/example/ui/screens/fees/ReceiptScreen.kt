package com.example.ui.screens.fees

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.PaymentSubmissionEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    submission: PaymentSubmissionEntity,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ডিজিটাল ফি রশিদ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পেছনে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize().testTag("receipt_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("printable_receipt_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(2.dp, RoyalBlue.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Logo & Institution Name
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_alams_logo),
                            contentDescription = "ALAM'S Logo",
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ALAM\'S Biology-Chemistry Private Batch",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = "Official Money Receipt • টিউশন ফি রশিদ",
                        style = MaterialTheme.typography.labelMedium.copy(color = BioTeal)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = BorderOutline)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Verified Stamp Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SuccessContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ভেরিফাইড ও গৃহীত (PAID)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Receipt Info Table
                    ReceiptField("রশিদ নম্বর (Receipt No)", submission.receiptNumber ?: "ALAM-REC-2026-0941")
                    ReceiptField("শিক্ষার্থীর নাম", submission.studentName)
                    ReceiptField("শ্রেণি ও ব্যাচ", "${submission.batch} ব্যাচ (Biology & Chemistry)")
                    ReceiptField("রোল নম্বর", submission.studentRoll)
                    ReceiptField("পেমেন্ট মেথড", "${submission.method} (Personal 01577654118)")
                    ReceiptField("Transaction ID (TrxID)", submission.trxId)
                    ReceiptField("প্রেরক ফোন নম্বর", submission.senderPhoneMasked)
                    ReceiptField("পরিশোধের পরিমাণ", "৳${submission.amount} (পরিশোধিত)")

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = BorderOutline)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Teacher Signature Note
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(text = "হেল্পলাইন: 01577654118", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                            Text(text = "YouTube: @alamssciencelab", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "আলম স্যার (ALAM Sir)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                            Text(
                                text = "অনুমোদিত স্বাক্ষর (Verified)",
                                style = MaterialTheme.typography.labelSmall.copy(color = SuccessGreen, fontSize = 10.sp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ফি তালিকায় ফিরে যান")
            }
        }
    }
}

@Composable
fun ReceiptField(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
