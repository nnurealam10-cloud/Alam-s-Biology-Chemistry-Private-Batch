package com.example.ui.screens.exams

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExamAttemptEntity
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.QuestionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.CurrentScreen
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultScreen(
    attempt: ExamAttemptEntity,
    exam: ExamEntity,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateTo(CurrentScreen.Exams) }

    val leaderboard by viewModel.getLeaderboard(exam.id).collectAsState(initial = emptyList())
    val questions by viewModel.activeExamQuestions.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: সারসংক্ষেপ ও র‍্যাংক, 1: প্রশ্নভিত্তিক সমাধান

    val userAnswers = remember(attempt.selectedAnswersJson) {
        val map = mutableMapOf<Int, Int>()
        try {
            val json = JSONObject(attempt.selectedAnswersJson)
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k.toInt()] = json.getInt(k)
            }
        } catch (_: Exception) {}
        map
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("পরীক্ষার ফলাফল ও র‍্যাংক") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(CurrentScreen.Exams) }) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize().testTag("exam_result_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("ফলাফল ও র‍্যাংক", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("সঠিক উত্তর পর্যালোচনা", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (selectedTab == 0) {
                // Score Summary & Leaderboard
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("result_summary_scroll"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hero Score Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(DeepNavy, RoyalBlue)
                                        )
                                    )
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = exam.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White.copy(alpha = 0.9f),
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "${attempt.score} / ${attempt.totalMarks}",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            color = ChemGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "অর্জিত সর্বমোট নম্বর",
                                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.8f))
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Stat pills row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        ResultStatPill(
                                            label = "সঠিক",
                                            value = "${attempt.correctCount}",
                                            color = BioEmerald
                                        )
                                        ResultStatPill(
                                            label = "ভুল",
                                            value = "${attempt.wrongCount}",
                                            color = ErrorRed
                                        )
                                        ResultStatPill(
                                            label = "অনুত্তর",
                                            value = "${attempt.unansweredCount}",
                                            color = Color.LightGray
                                        )
                                        ResultStatPill(
                                            label = "সময়",
                                            value = "${attempt.timeTakenSeconds / 60} মি",
                                            color = SoftBlueContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Live Batch Leaderboard Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "লাইভ ব্যাচ লিডারবোর্ড (${attempt.batch})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BioContainer
                            ) {
                                Text(
                                    text = "সরাসরি আপডেট",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = BioTeal,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Leaderboard list
                    itemsIndexed(leaderboard) { index, itemAttempt ->
                        val isCurrentStudent = itemAttempt.studentRoll == attempt.studentRoll
                        val rank = index + 1

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentStudent) SoftBlueContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isCurrentStudent) 2.dp else 1.dp,
                                if (isCurrentStudent) RoyalBlue else BorderOutline
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (rank) {
                                                    1 -> ChemGold
                                                    2 -> Color(0xFFC0C0C0)
                                                    3 -> Color(0xFFCD7F32)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$rank",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (rank <= 3) Color.White else TextPrimary
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = if (isCurrentStudent) "${itemAttempt.studentName} (আপনি)" else itemAttempt.studentName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isCurrentStudent) FontWeight.Bold else FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "রোল: ${itemAttempt.studentRoll.takeLast(4)} • সময়: ${itemAttempt.timeTakenSeconds / 60} মি ${itemAttempt.timeTakenSeconds % 60} সে",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                        )
                                    }
                                }

                                Text(
                                    text = "${itemAttempt.score} নম্বর",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentStudent) RoyalBlue else DeepNavy
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // Detailed Question Review
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("question_review_scroll"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(questions) { index, q ->
                        val selectedOpt = userAnswers[index]
                        val isCorrect = selectedOpt == q.correctOption
                        val isAnswered = selectedOpt != null

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
                                        text = "প্রশ্ন ${index + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            !isAnswered -> WarningContainer
                                            isCorrect -> SuccessContainer
                                            else -> ErrorContainer
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                !isAnswered -> "উত্তর দেওয়া হয়নি"
                                                isCorrect -> "সঠিক উত্তর (+১)"
                                                else -> "ভুল উত্তর (-০.২৫)"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    !isAnswered -> WarningOrange
                                                    isCorrect -> SuccessGreen
                                                    else -> ErrorRed
                                                }
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = q.questionText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                                options.forEachIndexed { optIndex, optText ->
                                    val isUserChoice = selectedOpt == optIndex
                                    val isRealCorrect = q.correctOption == optIndex

                                    val bg = when {
                                        isRealCorrect -> SuccessContainer
                                        isUserChoice && !isCorrect -> ErrorContainer
                                        else -> Color.Transparent
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = bg,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when {
                                                    isRealCorrect -> Icons.Default.CheckCircle
                                                    isUserChoice -> Icons.Default.Cancel
                                                    else -> Icons.Default.RadioButtonUnchecked
                                                },
                                                contentDescription = null,
                                                tint = when {
                                                    isRealCorrect -> SuccessGreen
                                                    isUserChoice -> ErrorRed
                                                    else -> TextMuted
                                                },
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = optText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = if (isRealCorrect) SuccessGreen else TextPrimary,
                                                    fontWeight = if (isRealCorrect || isUserChoice) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Scientific Explanation Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BioContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "আলম স্যারের ব্যাখ্যা:",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = BioTeal
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = q.explanation,
                                            style = MaterialTheme.typography.bodySmall.copy(color = DeepNavy)
                                        )
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
fun ResultStatPill(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        )
    }
}
