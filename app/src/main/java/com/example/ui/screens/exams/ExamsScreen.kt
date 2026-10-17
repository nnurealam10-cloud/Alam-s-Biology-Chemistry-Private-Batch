package com.example.ui.screens.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExamEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.CurrentScreen

@Composable
fun ExamsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val exams by viewModel.exams.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val currentBatch = profile?.batch ?: "SSC"

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("exams_screen")
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "অনলাইন পরীক্ষা ও মূল্যায়ন ($currentBatch ব্যাচ)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "নির্দিষ্ট সময়সীমার মধ্যে MCQ পরীক্ষা দিয়ে লাইভ র‍্যাংক ও সমাধান দেখুন",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Info card for rules
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftBlueContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "প্রতিটি ভুল উত্তরে ০.২৫ নম্বর কাটা যাবে। টাইমার শেষ হলে স্বয়ংক্রিয়ভাবে জমা হবে।",
                            style = MaterialTheme.typography.labelSmall.copy(color = DeepNavy)
                        )
                    }
                }
            }
        }

        Divider(color = BorderOutline)

        if (exams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "বর্তমানে কোনো পরীক্ষা সক্রিয় নেই",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("exam_list_scroll"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(exams, key = { it.id }) { exam ->
                    ExamCardItem(
                        exam = exam,
                        onStartExam = { viewModel.startExam(exam) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExamCardItem(
    exam: ExamEntity,
    onStartExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}"),
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
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (exam.subject == "BIOLOGY") BioContainer else ChemContainer
                ) {
                    Text(
                        text = if (exam.subject == "BIOLOGY") "জীববিজ্ঞান (Biology)" else "রসায়ন (Chemistry)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (exam.subject == "BIOLOGY") BioTeal else ChemAmber
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SuccessContainer
                ) {
                    Text(
                        text = "সক্রিয় পরীক্ষা",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exam.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "অধ্যায়: ${exam.chapter}",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${exam.durationMinutes} মিনিট", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = ChemGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("পূর্ণমান: ${exam.totalMarks}", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নেগেটিভ: ০.২৫", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onStartExam,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_exam_btn_${exam.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("পরীক্ষায় অংশ নিন (Start Exam)")
            }
        }
    }
}
