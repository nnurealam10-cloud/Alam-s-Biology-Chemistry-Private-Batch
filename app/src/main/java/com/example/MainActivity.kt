package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AlamsBottomNavigation
import com.example.ui.components.AlamsTopAppBar
import com.example.ui.screens.admin.AdminConsoleScreen
import com.example.ui.screens.community.CommunityScreen
import com.example.ui.screens.community.DiscussionThreadScreen
import com.example.ui.screens.exams.ExamResultScreen
import com.example.ui.screens.exams.ExamTakingScreen
import com.example.ui.screens.exams.ExamsScreen
import com.example.ui.screens.fees.FeesScreen
import com.example.ui.screens.fees.ReceiptScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.videos.VideoLibraryScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.CurrentScreen

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AlamsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AlamsApp(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var showBatchDialog by remember { mutableStateOf(false) }

    val currentBatch = profile?.batch ?: "SSC"
    val currentRole = profile?.role ?: "STUDENT"

    val isFullScreenFlow = currentScreen is CurrentScreen.ExamTaking ||
            currentScreen is CurrentScreen.ExamResult ||
            currentScreen is CurrentScreen.DiscussionThread ||
            currentScreen is CurrentScreen.ReceiptView ||
            currentScreen is CurrentScreen.AdminConsole

    Scaffold(
        topBar = {
            if (!isFullScreenFlow) {
                AlamsTopAppBar(
                    currentBatch = currentBatch,
                    currentRole = currentRole,
                    onBatchClick = { showBatchDialog = true },
                    onRoleToggle = { viewModel.toggleRole() },
                    onProfileClick = { showBatchDialog = true }
                )
            }
        },
        bottomBar = {
            if (!isFullScreenFlow) {
                AlamsBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        modifier = Modifier.fillMaxSize().testTag("main_scaffold")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullScreenFlow) PaddingValues(0.dp) else innerPadding)
        ) {
            when (val screen = currentScreen) {
                is CurrentScreen.Home -> HomeScreen(viewModel = viewModel)
                is CurrentScreen.Classes -> VideoLibraryScreen(viewModel = viewModel)
                is CurrentScreen.Exams -> ExamsScreen(viewModel = viewModel)
                is CurrentScreen.Community -> CommunityScreen(viewModel = viewModel)
                is CurrentScreen.Fees -> FeesScreen(viewModel = viewModel)
                is CurrentScreen.AdminConsole -> AdminConsoleScreen(viewModel = viewModel)
                is CurrentScreen.ExamTaking -> ExamTakingScreen(exam = screen.exam, viewModel = viewModel)
                is CurrentScreen.ExamResult -> ExamResultScreen(
                    attempt = screen.attempt,
                    exam = screen.exam,
                    viewModel = viewModel
                )
                is CurrentScreen.DiscussionThread -> DiscussionThreadScreen(
                    message = screen.message,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(CurrentScreen.Community) }
                )
                is CurrentScreen.ReceiptView -> ReceiptScreen(
                    submission = screen.submission,
                    onBack = { viewModel.navigateTo(CurrentScreen.Fees) }
                )
                else -> HomeScreen(viewModel = viewModel)
            }
        }
    }

    // Global Batch Switcher Dialog
    if (showBatchDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            title = {
                Text("আপনার বিজ্ঞান ব্যাচ নির্বাচন করুন", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ALAM'S Biology-Chemistry Private Batch-এর প্রতিটি শ্রেণির জন্য পৃথক ক্লাস, এক্সাম ও ডিসকাশন পরিচালিত হয়।",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (currentBatch == "SSC") RoyalBlue.copy(alpha = 0.12f) else Color.Transparent,
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
                                Text("SSC ব্যাচ (৯ম ও ১০ম শ্রেণি)", fontWeight = FontWeight.Bold, color = DeepNavy)
                                Text("জীববিজ্ঞান ও রসায়ন বেসিক টু অ্যাডভান্সড", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (currentBatch == "HSC") ChemAmber.copy(alpha = 0.12f) else Color.Transparent,
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
                                Text("HSC ব্যাচ (১১শ ও ১২শ শ্রেণি)", fontWeight = FontWeight.Bold, color = DeepNavy)
                                Text("জৈব রসায়ন, উদ্ভিদবিজ্ঞান ও প্রাণিবিজ্ঞান স্পেশাল", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBatchDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}
