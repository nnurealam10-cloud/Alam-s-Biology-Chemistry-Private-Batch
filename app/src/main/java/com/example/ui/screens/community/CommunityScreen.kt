package com.example.ui.screens.community

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DiscussionMessageEntity
import com.example.data.local.entity.DiscussionReplyEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.CurrentScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.discussionMessages.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var showNewQuestionDialog by remember { mutableStateOf(false) }
    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var reportingMessage by remember { mutableStateOf<DiscussionMessageEntity?>(null) }
    var reportReason by remember { mutableStateOf("") }

    val currentBatch = profile?.batch ?: "SSC"

    val filteredMessages = messages.filter { msg ->
        when (selectedSubjectFilter) {
            "BIOLOGY" -> msg.subject == "BIOLOGY"
            "CHEMISTRY" -> msg.subject == "CHEMISTRY"
            else -> true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("community_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewQuestionDialog = true },
                icon = { Icon(Icons.Default.AddComment, contentDescription = null) },
                text = { Text("প্রশ্ন করুন") },
                containerColor = RoyalBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("ask_question_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header: Batch isolation notice
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "সমস্যা সমাধান বক্স ($currentBatch ব্যাচ)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                            Text(
                                text = "জীববিজ্ঞান ও রসায়ন প্রশ্ন জিজ্ঞাসা ও সহপাঠী আলোচনা",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BioContainer
                        ) {
                            Text(
                                text = "মডারেটেড রুম",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BioTeal,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subject Filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedSubjectFilter == "ALL",
                            onClick = { selectedSubjectFilter = "ALL" },
                            label = { Text("সকল প্রশ্ন") }
                        )
                        FilterChip(
                            selected = selectedSubjectFilter == "BIOLOGY",
                            onClick = { selectedSubjectFilter = "BIOLOGY" },
                            label = { Text("জীববিজ্ঞান") },
                            leadingIcon = {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = BioTeal, modifier = Modifier.size(16.dp))
                            }
                        )
                        FilterChip(
                            selected = selectedSubjectFilter == "CHEMISTRY",
                            onClick = { selectedSubjectFilter = "CHEMISTRY" },
                            label = { Text("রসায়ন") },
                            leadingIcon = {
                                Icon(Icons.Default.Science, contentDescription = null, tint = ChemAmber, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            Divider(color = BorderOutline)

            // Message list
            if (filteredMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "কোনো প্রশ্ন বা আলোচনা পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "নিচের বোতাম চেপে প্রথম প্রশ্নটি করুন!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("discussion_list_scroll"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        DiscussionMessageCard(
                            message = msg,
                            onClick = { viewModel.selectDiscussionMessage(msg) },
                            onReport = { reportingMessage = msg }
                        )
                    }
                }
            }
        }
    }

    // New Question Dialog
    if (showNewQuestionDialog) {
        var questionTitle by remember { mutableStateOf("") }
        var questionBody by remember { mutableStateOf("") }
        var selectedSubject by remember { mutableStateOf("BIOLOGY") }
        var chapterName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewQuestionDialog = false },
            title = { Text("নতুন প্রশ্ন বা সংশয় পোস্ট করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Subject radio selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedSubject == "BIOLOGY") BioContainer else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedSubject == "BIOLOGY") BioTeal else BorderOutline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSubject = "BIOLOGY" }
                        ) {
                            Text(
                                text = "জীববিজ্ঞান (Biology)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedSubject == "BIOLOGY") BioTeal else TextSecondary
                                ),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedSubject == "CHEMISTRY") ChemContainer else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedSubject == "CHEMISTRY") ChemAmber else BorderOutline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSubject = "CHEMISTRY" }
                        ) {
                            Text(
                                text = "রসায়ন (Chemistry)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedSubject == "CHEMISTRY") ChemAmber else TextSecondary
                                ),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = chapterName,
                        onValueChange = { chapterName = it },
                        label = { Text("অধ্যায় বা টপিক (উদা: কোষ বিভাজন, পর্যায় সারণি)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = questionTitle,
                        onValueChange = { questionTitle = it },
                        label = { Text("প্রশ্নের শিরোনাম") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = questionBody,
                        onValueChange = { questionBody = it },
                        label = { Text("বিস্তারিত বিবরণ লিখুন") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (questionTitle.isNotBlank() && questionBody.isNotBlank()) {
                            viewModel.postQuestion(
                                subject = selectedSubject,
                                chapter = if (chapterName.isBlank()) "সাধারণ" else chapterName,
                                title = questionTitle,
                                question = questionBody
                            )
                            showNewQuestionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("পোস্ট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewQuestionDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Report Dialog
    reportingMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { reportingMessage = null },
            title = { Text("পোস্টটি রিপোর্ট করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("পোস্টটি কেন অনুপযুক্ত মনে হচ্ছে তা লিখুন। মডারেটর এটি খতিয়ে দেখবেন।", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("স্প্যাম, অশালীন বা অনাকাঙ্ক্ষিত বিষয়বস্তু...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isNotBlank()) {
                            viewModel.reportDiscussionMessage(msg.id, reportReason)
                            reportingMessage = null
                            reportReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("রিপোর্ট জমা দিন")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportingMessage = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun DiscussionMessageCard(
    message: DiscussionMessageEntity,
    onClick: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("discussion_card_${message.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Author and tag row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (message.authorRole == "TEACHER") RoyalBlue else SoftBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.authorName.take(1),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (message.authorRole == "TEACHER") Color.White else DeepNavy
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = message.authorName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = if (message.authorRole == "TEACHER") "শিক্ষক (ALAM Sir)" else "শিক্ষার্থী",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (message.authorRole == "TEACHER") RoyalBlue else TextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (message.subject == "BIOLOGY") BioContainer else ChemContainer
                ) {
                    Text(
                        text = if (message.subject == "BIOLOGY") "জীববিজ্ঞান" else "রসায়ন",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (message.subject == "BIOLOGY") BioTeal else ChemAmber,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = message.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message.question,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    lineHeight = 20.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "অধ্যায়: ${message.chapter}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${message.replyCount} টি উত্তর",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = onReport,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = "রিপোর্ট",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
