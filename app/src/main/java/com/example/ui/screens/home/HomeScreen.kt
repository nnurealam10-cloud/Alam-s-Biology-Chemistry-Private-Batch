package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.FeeInvoiceEntity
import com.example.data.local.entity.NoticeEntity
import com.example.data.local.entity.VideoLessonEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.CurrentScreen

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val invoices by viewModel.studentInvoices.collectAsState()

    var selectedNoticeForDialog by remember { mutableStateOf<NoticeEntity?>(null) }
    var showBatchDialog by remember { mutableStateOf(false) }

    val currentBatch = profile?.batch ?: "SSC"
    val latestNotice = notices.firstOrNull()
    val upcomingExam = exams.firstOrNull { it.status == "ACTIVE" } ?: exams.firstOrNull()
    val latestVideo = videos.firstOrNull()
    val pendingInvoice = invoices.firstOrNull { it.status == "UNPAID" } ?: invoices.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Greeting Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_greeting_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(DeepNavy, RoyalBlue)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "স্বাগতম, ${profile?.name ?: "শিক্ষার্থী"}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "রোল: ${profile?.roll}  •  ${if (currentBatch == "SSC") "SSC ব্যাচ (৯ম-১০ম)" else "HSC ব্যাচ (১১শ-১২শ)"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = BioTealLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            // Quick Batch Changer Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .testTag("change_batch_quick_button")
                                    .clickable { showBatchDialog = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Change Batch",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ব্যাচ পরিবর্তন",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick info pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.25f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = ChemAmberLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "YouTube: @alamssciencelab | বিকাশ/নগদ: 01577654118",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Quick Action Shortcut Grid
        item {
            Text(
                text = "দ্রুত সেবা ও কার্যক্রম",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "অনলাইন পরীক্ষা",
                    subtitle = "মডেল টেস্ট",
                    icon = Icons.Default.Quiz,
                    color = RoyalBlue,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(CurrentScreen.Exams)
                }

                QuickActionItem(
                    title = "ভিডিও ক্লাস",
                    subtitle = "অধ্যায়ভিত্তিক",
                    icon = Icons.Default.PlayCircleOutline,
                    color = BioTeal,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(CurrentScreen.Classes)
                }

                QuickActionItem(
                    title = "প্রশ্ন বক্স",
                    subtitle = "সমাধান ফোরাম",
                    icon = Icons.Default.HelpOutline,
                    color = ChemAmber,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(CurrentScreen.Community)
                }

                QuickActionItem(
                    title = "বেতন পরিশোধ",
                    subtitle = "বিকাশ / নগদ",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(CurrentScreen.Fees)
                }
            }
        }

        // 3. Urgent Notice Section
        if (latestNotice != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_card_${latestNotice.id}")
                        .clickable {
                            selectedNoticeForDialog = latestNotice
                            viewModel.markNoticeRead(latestNotice.id)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (latestNotice.category == "URGENT") ErrorContainer else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (latestNotice.category == "URGENT") "জরুরি নোটিশ" else "নোটিশ বোর্ড",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (latestNotice.category == "URGENT") ErrorRed else RoyalBlue
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                if (!latestNotice.isRead) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(ErrorRed)
                                    )
                                }
                            }

                            Text(
                                text = "বিস্তারিত দেখুন →",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = latestNotice.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = latestNotice.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                lineHeight = 20.sp
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. Upcoming / Active Exam Card
        if (upcomingExam != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upcoming_exam_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "অনলাইন পরীক্ষা ও লাইভ র‍্যাংক",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (upcomingExam.subject == "BIOLOGY") BioContainer else ChemContainer
                            ) {
                                Text(
                                    text = if (upcomingExam.subject == "BIOLOGY") "জীববিজ্ঞান" else "রসায়ন",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (upcomingExam.subject == "BIOLOGY") BioTeal else ChemAmber,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = upcomingExam.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "অধ্যায়: ${upcomingExam.chapter}  •  পূর্ণমান: ${upcomingExam.totalMarks}  •  সময়: ${upcomingExam.durationMinutes} মিনিট",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.startExam(upcomingExam) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_exam_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("পরীক্ষা শুরু করুন (Start Exam)")
                        }
                    }
                }
            }
        }

        // 5. Recent Class Video Card
        if (latestVideo != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("latest_video_card")
                        .clickable { viewModel.navigateTo(CurrentScreen.Classes) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SmartDisplay,
                                    contentDescription = null,
                                    tint = Color(0xFFFF0000), // YouTube Red
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সর্বশেষ ক্লাস (@alamssciencelab)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }

                            Text(
                                text = "${latestVideo.durationMinutes} মিনিট",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = latestVideo.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = latestVideo.chapter,
                            style = MaterialTheme.typography.bodySmall.copy(color = BioTeal)
                        )
                    }
                }
            }
        }

        // 6. Fee Payment Status Card
        if (pendingInvoice != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fee_status_card")
                        .clickable { viewModel.navigateTo(CurrentScreen.Fees) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (pendingInvoice.status == "PAID") SuccessContainer else WarningContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "মাসিক বেতন: ${pendingInvoice.monthYear}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (pendingInvoice.status == "PAID") "পরিশোধিত (রশিদ সংরক্ষিত)" else "বকেয়া: ৳${pendingInvoice.amount} (শেষ তারিখ: ${pendingInvoice.dueDate})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (pendingInvoice.status == "PAID") SuccessGreen else WarningOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateTo(CurrentScreen.Fees) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (pendingInvoice.status == "PAID") SuccessGreen else WarningOrange
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (pendingInvoice.status == "PAID") "রশিদ দেখুন" else "পরিশোধ করুন")
                        }
                    }
                }
            }
        }
    }

    // Notice Detail Dialog
    selectedNoticeForDialog?.let { notice ->
        AlertDialog(
            onDismissRequest = { selectedNoticeForDialog = null },
            confirmButton = {
                TextButton(onClick = { selectedNoticeForDialog = null }) {
                    Text("বন্ধ করুন")
                }
            },
            title = {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = notice.content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                )
            }
        )
    }

    // Batch Change Dialog
    if (showBatchDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            title = {
                Text("আপনার ব্যাচ নির্বাচন করুন", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "ALAM'S Biology-Chemistry Private Batch-এ SSC ও HSC-এর ক্লাস, পরীক্ষা, নোটিশ ও সমাধান ফোরাম সম্পূর্ণ আলাদা থাকে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (currentBatch == "SSC") RoyalBlue.copy(alpha = 0.15f) else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentBatch == "SSC") RoyalBlue else BorderOutline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.switchBatch("SSC")
                                showBatchDialog = false
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            RadioButton(
                                selected = currentBatch == "SSC",
                                onClick = {
                                    viewModel.switchBatch("SSC")
                                    showBatchDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("SSC ব্যাচ (৯ম ও ১০ম শ্রেণি)", fontWeight = FontWeight.Bold)
                                Text("জীববিজ্ঞান ও রসায়ন স্পেশাল ফাউন্ডেশন", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (currentBatch == "HSC") ChemAmber.copy(alpha = 0.15f) else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentBatch == "HSC") ChemAmber else BorderOutline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.switchBatch("HSC")
                                showBatchDialog = false
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            RadioButton(
                                selected = currentBatch == "HSC",
                                onClick = {
                                    viewModel.switchBatch("HSC")
                                    showBatchDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("HSC ব্যাচ (১১শ ও ১২শ শ্রেণি)", fontWeight = FontWeight.Bold)
                                Text("জৈব রসায়ন, উদ্ভিদ ও প্রাণিবিজ্ঞান অ্যাডভান্সড", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBatchDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun QuickActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
