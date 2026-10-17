package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.CurrentScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlamsTopAppBar(
    currentBatch: String,
    currentRole: String,
    onBatchClick: () -> Unit,
    onRoleToggle: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Institution Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DeepNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_alams_logo),
                            contentDescription = "ALAM'S Logo",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "ALAM\'S Private Batch",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                letterSpacing = 0.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Biology & Chemistry Excellence",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BioTeal,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Batch Switcher Chip & Admin Mode Switcher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Batch Badge Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (currentBatch == "SSC") RoyalBlue.copy(alpha = 0.12f) else ChemAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentBatch == "SSC") RoyalBlue else ChemAmber
                        ),
                        modifier = Modifier
                            .testTag("batch_switcher_chip")
                            .clickable { onBatchClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = if (currentBatch == "SSC") RoyalBlue else ChemAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentBatch,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentBatch == "SSC") RoyalBlue else ChemAmber
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = if (currentBatch == "SSC") RoyalBlue else ChemAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Role Switcher Button
                    IconButton(
                        onClick = onRoleToggle,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("role_toggle_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (currentRole == "ADMIN") ErrorRed else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (currentRole == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = "Switch Role",
                                    tint = if (currentRole == "ADMIN") Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlamsBottomNavigation(
    currentScreen: CurrentScreen,
    onNavigate: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val isHome = currentScreen is CurrentScreen.Home
        val isClasses = currentScreen is CurrentScreen.Classes
        val isExams = currentScreen is CurrentScreen.Exams || currentScreen is CurrentScreen.ExamTaking || currentScreen is CurrentScreen.ExamResult
        val isCommunity = currentScreen is CurrentScreen.Community || currentScreen is CurrentScreen.DiscussionThread
        val isFees = currentScreen is CurrentScreen.Fees || currentScreen is CurrentScreen.ReceiptView

        NavigationBarItem(
            selected = isHome,
            onClick = { onNavigate(CurrentScreen.Home) },
            icon = {
                Icon(
                    imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "হোম"
                )
            },
            label = { Text("হোম", maxLines = 1) },
            modifier = Modifier.testTag("nav_home_button")
        )

        NavigationBarItem(
            selected = isClasses,
            onClick = { onNavigate(CurrentScreen.Classes) },
            icon = {
                Icon(
                    imageVector = if (isClasses) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                    contentDescription = "ক্লাস"
                )
            },
            label = { Text("ক্লাস", maxLines = 1) },
            modifier = Modifier.testTag("nav_classes_button")
        )

        NavigationBarItem(
            selected = isExams,
            onClick = { onNavigate(CurrentScreen.Exams) },
            icon = {
                Icon(
                    imageVector = if (isExams) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                    contentDescription = "পরীক্ষা"
                )
            },
            label = { Text("পরীক্ষা", maxLines = 1) },
            modifier = Modifier.testTag("nav_exams_button")
        )

        NavigationBarItem(
            selected = isCommunity,
            onClick = { onNavigate(CurrentScreen.Community) },
            icon = {
                Icon(
                    imageVector = if (isCommunity) Icons.Filled.Forum else Icons.Outlined.Forum,
                    contentDescription = "সমাধান"
                )
            },
            label = { Text("সমাধান", maxLines = 1) },
            modifier = Modifier.testTag("nav_community_button")
        )

        NavigationBarItem(
            selected = isFees,
            onClick = { onNavigate(CurrentScreen.Fees) },
            icon = {
                Icon(
                    imageVector = if (isFees) Icons.Filled.Payments else Icons.Outlined.Payments,
                    contentDescription = "বেতন ও ফি"
                )
            },
            label = { Text("ফি", maxLines = 1) },
            modifier = Modifier.testTag("nav_fees_button")
        )
    }
}
