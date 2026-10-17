package com.example.ui.screens.admin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PaymentSubmissionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.CurrentScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConsoleScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { viewModel.navigateTo(CurrentScreen.Home) }

    val pendingSubmissions by viewModel.pendingSubmissionsForAdmin.collectAsState()
    val reportedMessages by viewModel.reportedMessages.collectAsState()
    val allNotices by viewModel.allNoticesForAdmin.collectAsState()

    var selectedAdminTab by remember { mutableStateOf(0) } // 0: ফি ভেরিফিকেশন, 1: নোটিশ প্রকাশ, 2: ভিডিও ক্লাস যোগ, 3: মডারেশন

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("শিক্ষক ও অ্যাডমিন কনসোল", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("ALAM'S Science Lab Management", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(CurrentScreen.Home) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "হোমে ফিরে যান")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleRole() }) {
                        Text("ছাত্র মোডে ফিরুন", fontWeight = FontWeight.Bold, color = RoyalBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize().testTag("admin_console_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Admin Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ফি ভেরিফিকেশন")
                            if (pendingSubmissions.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge { Text("${pendingSubmissions.size}") }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = { Text("নতুন নোটিশ") }
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = { Text("ভিডিও লেকচার") }
                )
                Tab(
                    selected = selectedAdminTab == 3,
                    onClick = { selectedAdminTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("রিপোর্ট ও মডারেশন")
                            if (reportedMessages.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge { Text("${reportedMessages.size}") }
                            }
                        }
                    }
                )
            }

            when (selectedAdminTab) {
                0 -> {
                    // Fee Verification Queue
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("admin_fee_queue"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "পেন্ডিং বিকাশ ও নগদ ফি যাচাই তালিকা (${pendingSubmissions.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (pendingSubmissions.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "বর্তমানে কোনো পেন্ডিং ফি সাবমিশন নেই। সব যাচাইকৃত!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        } else {
                            items(pendingSubmissions, key = { it.id }) { sub ->
                                AdminFeeVerificationCard(
                                    submission = sub,
                                    onApprove = {
                                        viewModel.verifyPayment(sub.id, sub.invoiceId, "বিকাশ/নগদ লেনদেন যাচাই করে গৃহীত হলো।")
                                        Toast.makeText(context, "${sub.studentName}-এর পেমেন্ট গৃহীত হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                    onReject = { reason ->
                                        viewModel.rejectPayment(sub.id, sub.invoiceId, reason)
                                        Toast.makeText(context, "পেমেন্টটি বাতিল করা হয়েছে।", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Notice Composer
                    NoticeComposerTab(
                        onPublish = { title, content, batch, category, isPinned ->
                            viewModel.createNotice(title, content, batch, category, isPinned)
                            Toast.makeText(context, "নোটিশ সফলভাবে প্রকাশিত হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                2 -> {
                    // Video Registration
                    VideoRegistrationTab(
                        onRegister = { title, subject, batch, chapter, url, duration ->
                            viewModel.registerVideoLesson(title, subject, batch, chapter, url, duration)
                            Toast.makeText(context, "ভিডিও ক্লাস যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                3 -> {
                    // Moderation Queue
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("admin_moderation_queue"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "রিপোর্টকৃত পোস্ট ও শিক্ষার্থী নিরাপত্তা পর্যালোচনা (${reportedMessages.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (reportedMessages.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "কোনো রিপোর্টকৃত আলোচনা নেই। কমিউনিটি পরিচ্ছন্ন!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BioTeal
                                    )
                                }
                            }
                        } else {
                            items(reportedMessages, key = { it.id }) { msg ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(text = "পোস্টকারী: ${msg.authorName} (${msg.batch} ব্যাচ)", fontWeight = FontWeight.Bold)
                                        Text(text = "রিপোর্টের কারণ: ${msg.reportReason ?: "অনুপযুক্ত বিষয়বস্তু"}", color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = msg.title, fontWeight = FontWeight.SemiBold)
                                        Text(text = msg.question, style = MaterialTheme.typography.bodySmall)

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.dismissMessageReport(msg.id) }
                                            ) {
                                                Text("রিপোর্ট খারিজ করুন")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { viewModel.deleteDiscussionMessage(msg.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                                            ) {
                                                Text("পোস্ট মুছে ফেলুন")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFeeVerificationCard(
    submission: PaymentSubmissionEntity,
    onApprove: () -> Unit,
    onReject: (String) -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReasonInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = "${submission.studentName} (${submission.batch})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WarningContainer
                ) {
                    Text(
                        text = "পেন্ডিং",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = WarningOrange),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = "রোল: ${submission.studentRoll} • প্রেরক নম্বর: ${submission.senderPhoneMasked}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(text = "মাধ্যম: ${submission.method} • TrxID: ${submission.trxId}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = RoyalBlue))
            Text(text = "টাকার পরিমাণ: ৳${submission.amount}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavy))

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Text("বাতিল করুন")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onApprove,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("যাচাই ও অনুমোদন করুন")
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("বাতিলের কারণ লিখুন", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = rejectReasonInput,
                    onValueChange = { rejectReasonInput = it },
                    placeholder = { Text("উদা: TrxID মিলেনি বা ভুল পরিমাণ") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(if (rejectReasonInput.isBlank()) "TrxID সঠিক পাওয়া যায়নি।" else rejectReasonInput)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun NoticeComposerTab(
    onPublish: (title: String, content: String, batch: String, category: String, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetBatch by remember { mutableStateOf("ALL") } // ALL, SSC, HSC
    var category by remember { mutableStateOf("GENERAL") } // URGENT, EXAM, CLASS, PAYMENT, GENERAL
    var isPinned by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("নতুন নোটিশ রচনা ও প্রকাশ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("নোটিশের শিরোনাম") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("নোটিশের পূর্ণাঙ্গ বিবরণ") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                maxLines = 6
            )
        }

        item {
            Text("টার্গেট ব্যাচ নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "সকল ব্যাচ", "SSC" to "SSC ব্যাচ", "HSC" to "HSC ব্যাচ").forEach { (code, label) ->
                    FilterChip(
                        selected = targetBatch == code,
                        onClick = { targetBatch = code },
                        label = { Text(label) }
                    )
                }
            }
        }

        item {
            Text("ক্যাটাগরি:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("URGENT" to "জরুরি", "EXAM" to "পরীক্ষা", "CLASS" to "ক্লাস", "PAYMENT" to "বেতন").forEach { (code, label) ->
                    FilterChip(
                        selected = category == code,
                        onClick = { category = code },
                        label = { Text(label) }
                    )
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                Spacer(modifier = Modifier.width(6.dp))
                Text("শীর্ষে পিন করে রাখুন (Pin Notice)")
            }
        }

        item {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onPublish(title, content, targetBatch, category, isPinned)
                        title = ""
                        content = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Icon(Icons.Default.Publish, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("নোটিশ প্রকাশ করুন")
            }
        }
    }
}

@Composable
fun VideoRegistrationTab(
    onRegister: (title: String, subject: String, batch: String, chapter: String, url: String, duration: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("BIOLOGY") }
    var batch by remember { mutableStateOf("SSC") }
    var chapter by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("40") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("YouTube ক্লাস ভিডিও রেজিস্ট্রেশন (@alamssciencelab)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("ভিডিওর YouTube লিঙ্ক বা Video ID দিয়ে অ্যাপের লাইব্রেরিতে অন্তর্ভুক্ত করুন।", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("ভিডিও ক্লাসের শিরোনাম") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = youtubeUrl,
                onValueChange = { youtubeUrl = it },
                label = { Text("YouTube Video URL বা Video ID") },
                placeholder = { Text("উদা: https://youtube.com/watch?v=XXXX বা Video ID") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = chapter,
                onValueChange = { chapter = it },
                label = { Text("অধ্যায় বা পাঠ্যসূচি (উদা: কোষ রসায়ন, জৈব রসায়ন)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("বিষয়:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Row {
                        FilterChip(selected = subject == "BIOLOGY", onClick = { subject = "BIOLOGY" }, label = { Text("জীববিজ্ঞান") })
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(selected = subject == "CHEMISTRY", onClick = { subject = "CHEMISTRY" }, label = { Text("রসায়ন") })
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("ব্যাচ:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Row {
                        FilterChip(selected = batch == "SSC", onClick = { batch = "SSC" }, label = { Text("SSC") })
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(selected = batch == "HSC", onClick = { batch = "HSC" }, label = { Text("HSC") })
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("ভিডিওর সময়সীমা (মিনিট)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    if (title.isNotBlank() && youtubeUrl.isNotBlank()) {
                        val durInt = duration.toIntOrNull() ?: 35
                        onRegister(title, subject, batch, if (chapter.isBlank()) "সাধারণ ক্লাস" else chapter, youtubeUrl, durInt)
                        title = ""
                        youtubeUrl = ""
                        chapter = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("লাইব্রেরিতে ক্লাস যুক্ত করুন")
            }
        }
    }
}
