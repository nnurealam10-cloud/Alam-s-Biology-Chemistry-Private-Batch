package com.example.ui.screens.exams

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExamEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@Composable
fun ExamTakingScreen(
    exam: ExamEntity,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val questions by viewModel.activeExamQuestions.collectAsState()
    val answers by viewModel.selectedAnswers.collectAsState()
    val remainingSeconds by viewModel.remainingTimeSeconds.collectAsState()

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept hardware back button to prevent accidental exit
    BackHandler {
        showExitDialog = true
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val isTimeLow = remainingSeconds < 120

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exam.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Text(
                                text = "প্রশ্ন: ${currentQuestionIndex + 1} / ${questions.size}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }

                        // Countdown Timer Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isTimeLow) ErrorContainer else SoftBlueContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isTimeLow) ErrorRed else RoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = timeFormatted,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTimeLow) ErrorRed else RoyalBlue
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Question quick navigator pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(questions) { idx, _ ->
                            val isAnswered = answers.containsKey(idx)
                            val isCurrent = idx == currentQuestionIndex

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> RoyalBlue
                                            isAnswered -> BioTeal
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurrent || isAnswered) Color.Transparent else BorderOutline,
                                        CircleShape
                                    )
                                    .clickable { currentQuestionIndex = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent || isAnswered) Color.White else TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentQuestionIndex > 0) currentQuestionIndex--
                        },
                        enabled = currentQuestionIndex > 0,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("পূর্ববর্তী")
                    }

                    if (currentQuestionIndex < questions.size - 1) {
                        Button(
                            onClick = { currentQuestionIndex++ },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text("পরবর্তী")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { showSubmitDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.testTag("submit_exam_action_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("জমা দিন (Submit)")
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize().testTag("exam_taking_screen")
    ) { padding ->
        if (currentQuestion != null) {
            val selectedOption = answers[currentQuestionIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Question Text Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text(
                                text = "প্রশ্ন নং ${currentQuestionIndex + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue
                                )
                            )
                            Text(
                                text = "১ নম্বর",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }

                // Options (A, B, C, D)
                val options = listOf(
                    "ক) " + currentQuestion.optionA,
                    "খ) " + currentQuestion.optionB,
                    "গ) " + currentQuestion.optionC,
                    "ঘ) " + currentQuestion.optionD
                )

                options.forEachIndexed { optIndex, text ->
                    val isSelected = selectedOption == optIndex

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("option_${currentQuestionIndex}_$optIndex")
                            .clickable {
                                viewModel.selectAnswer(currentQuestionIndex, optIndex)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SoftBlueContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) RoyalBlue else BorderOutline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectAnswer(currentQuestionIndex, optIndex) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) DeepNavy else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitDialog) {
        val answeredCount = answers.size
        val unansweredCount = questions.size - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("পরীক্ষা জমা দিতে চান?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("আপনি মোট ${questions.size} টি প্রশ্নের মধ্যে $answeredCount টির উত্তর দিয়েছেন।")
                    if (unansweredCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "সতর্কতা: এখনো $unansweredCount টি প্রশ্নের উত্তর দেওয়া বাকি আছে!",
                            color = WarningOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("জমা দেওয়ার পর ফলাফল, নির্ভুল নম্বর এবং লাইভ র‍্যাংক দেখতে পাবেন।")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitExam(exam)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("হ্যাঁ, জমা দিন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("পরীক্ষা চালিয়ে যান")
                }
            }
        )
    }

    // Exit Warning Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("পরীক্ষা থেকে বের হবেন?", fontWeight = FontWeight.Bold) },
            text = { Text("এখন বের হয়ে গেলে বর্তমান উত্তরসমূহ স্বয়ংক্রিয়ভাবে জমা হবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        viewModel.submitExam(exam)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("জমা দিয়ে বের হন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("পরীক্ষায় থাকুন")
                }
            }
        )
    }
}
