package com.example.ui.screens.videos

import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.VideoLessonEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLibraryScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videos by viewModel.videos.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("ALL") } // ALL, BIOLOGY, CHEMISTRY
    var showBookmarksOnly by remember { mutableStateOf(false) }
    var playingVideo by remember { mutableStateOf<VideoLessonEntity?>(null) }

    val currentBatch = profile?.batch ?: "SSC"

    val filteredVideos = videos.filter { video ->
        val matchesSearch = video.title.contains(searchQuery, ignoreCase = true) ||
                video.chapter.contains(searchQuery, ignoreCase = true)
        val matchesSubject = when (selectedSubjectFilter) {
            "BIOLOGY" -> video.subject == "BIOLOGY"
            "CHEMISTRY" -> video.subject == "CHEMISTRY"
            else -> true
        }
        val matchesBookmark = !showBookmarksOnly || video.isBookmarked
        matchesSearch && matchesSubject && matchesBookmark
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("video_library_screen")
    ) {
        // Channel Header Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF0000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "YouTube",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "@alamssciencelab",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "ALAM'S Science Lab • $currentBatch ভিডিও লেকচার",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    // Open in YouTube Button
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://youtube.com/@alamssciencelab?si=VqUKOKwjWAbw3ThV")
                            )
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("চ্যানেল", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("অধ্যায় বা বিষয়ের নাম খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "মুছুন")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subject Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSubjectFilter == "ALL",
                        onClick = { selectedSubjectFilter = "ALL" },
                        label = { Text("সকল ক্লাস") }
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

        // Video List
        if (filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "কোনো ক্লাস ভিডিও পাওয়া যায়নি",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("video_list_scroll"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredVideos, key = { it.id }) { video ->
                    VideoItemCard(
                        video = video,
                        onPlayClick = { playingVideo = video },
                        onBookmarkToggle = { viewModel.toggleVideoBookmark(video.id, video.isBookmarked) },
                        onOpenExternal = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(video.youtubeUrl)
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    // Interactive Video Player Modal
    playingVideo?.let { video ->
        Dialog(onDismissRequest = { playingVideo = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = { playingVideo = null }) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Embedded Web Player
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    webChromeClient = WebChromeClient()
                                    webViewClient = WebViewClient()
                                    val embedHtml = """
                                        <!DOCTYPE html>
                                        <html>
                                        <body style="margin:0;padding:0;background-color:#000;">
                                          <iframe width="100%" height="100%" src="https://www.youtube.com/embed/${video.youtubeVideoId}?autoplay=1&rel=0" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
                                        </body>
                                        </html>
                                    """.trimIndent()
                                    loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "অধ্যায়: ${video.chapter}  •  সময়: ${video.durationMinutes} মিনিট",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=${video.youtubeVideoId}")
                                )
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("YouTube অ্যাপে দেখুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoItemCard(
    video: VideoLessonEntity,
    onPlayClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onOpenExternal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_card_${video.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Subject Tag, Chapter, and Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (video.subject == "BIOLOGY") BioContainer else ChemContainer
                ) {
                    Text(
                        text = if (video.subject == "BIOLOGY") "জীববিজ্ঞান (Biology)" else "রসায়ন (Chemistry)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (video.subject == "BIOLOGY") BioTeal else ChemAmber
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (video.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "বুকমার্ক",
                        tint = if (video.isBookmarked) ChemGold else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = video.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = video.chapter,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${video.durationMinutes} মিনিট  •  ${video.publishDate}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("play_video_button_${video.id}")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ক্লাস দেখুন")
                    }
                }
            }
        }
    }
}
