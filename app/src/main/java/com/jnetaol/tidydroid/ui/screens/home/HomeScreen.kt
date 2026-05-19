package com.jnetaol.tidydroid.ui.screens.home

import android.os.Build
import android.os.Environment
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.tidydroid.data.model.ScanResult
import com.jnetaol.tidydroid.ui.components.*
import com.jnetaol.tidydroid.ui.screens.AppViewModel
import com.jnetaol.tidydroid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToRules: () -> Unit,
    onNavigateToDuplicates: () -> Unit,
    onNavigateToCleaner: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scanResults by viewModel.scanResults.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val needsPermission by viewModel.needsStoragePermission.collectAsState()
    val stats = viewModel.statsFormatted

    Column(Modifier.fillMaxSize().background(TDBackground)) {
        Row(Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("TidyDroid", color = TDTextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Smart Download Organiser", color = TDTextMuted, fontSize = 13.sp)
            }
            IconButton(onNavigateToSettings) {
                Icon(Icons.Default.Settings, null, tint = TDTextSecondary)
            }
        }

        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (needsPermission) {
                item {
                    NeonCard(borderColor = TDWarning.copy(alpha = 0.5f)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Storage Permission Required", color = TDWarning, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("TidyDroid needs file management access to organize your downloads.", color = TDTextSecondary, fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            GlowButton("Grant", Icons.Default.LockOpen, glowColor = TDWarning, enabled = true, onClick = {
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                        intent.data = android.net.Uri.parse("package:${context.packageName}")
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                    context.startActivity(intent)
                                }
                            })
                        }
                    }
                }
            }

            item {
                NeonCard(borderColor = TDPrimary.copy(alpha = 0.3f)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        GlowButton(
                            if (isScanning) "Scanning\u2026" else "Organize Now",
                            if (isScanning) Icons.Default.HourglassTop else Icons.Default.AutoAwesome,
                            glowColor = TDPrimary,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isScanning,
                            onClick = { viewModel.organizeNow() }
                        )
                        if (isScanning) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = TDPrimary, trackColor = TDSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Quick Access")
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatsCard(stats["organized"] ?: "0", "Files Organized", Icons.Default.Folder, TDPrimary, Modifier.weight(1f))
                    StatsCard(stats["scans"] ?: "0", "Total Scans", Icons.Default.Assessment, TDSecondary, Modifier.weight(1f))
                    StatsCard(stats["spaceFreed"] ?: "0 B", "Space Freed", Icons.Default.Storage, TDNeonGreen, Modifier.weight(1f))
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        Modifier.weight(1f).clickable(onClick = onNavigateToDuplicates),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TDCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TDNeonOrange.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ContentCopy, null, Modifier.size(28.dp), tint = TDNeonOrange)
                            Spacer(Modifier.height(8.dp))
                            Text("Duplicates", color = TDTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(stats["duplicates"] ?: "0", color = TDNeonOrange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        Modifier.weight(1f).clickable(onClick = onNavigateToCleaner),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TDCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TDNeonRed.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DeleteSweep, null, Modifier.size(28.dp), tint = TDNeonRed)
                            Spacer(Modifier.height(8.dp))
                            Text("Large Files", color = TDTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(stats["largeFiles"] ?: "0", color = TDNeonRed, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        Modifier.weight(1f).clickable(onClick = onNavigateToRules),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TDCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TDSecondary.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Rule, null, Modifier.size(28.dp), tint = TDSecondary)
                            Spacer(Modifier.height(8.dp))
                            Text("Rules", color = TDTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text((scanResults.size).toString(), color = TDSecondary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                SectionHeader("Recent Scans")
            }

            if (scanResults.isEmpty()) {
                item { EmptyState(Icons.Default.Assessment, "No scans yet", "Tap Organize Now to start sorting your downloads") }
            } else {
                items(scanResults.take(5), key = { it.id }) { result ->
                    ScanHistoryCard(result = result, onDelete = { viewModel.deleteScanResult(result) })
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ScanHistoryCard(result: ScanResult, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)

    NeonCard {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(dateFormat.format(Date(result.timestamp)), color = TDTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${result.filesMoved}/${result.filesFound}", color = TDPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    IconButton({ showDelete = true }, Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = TDTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (result.videosCount > 0) CategoryBadge("Videos ${result.videosCount}")
                if (result.apksCount > 0) CategoryBadge("APKs ${result.apksCount}")
                if (result.musicCount > 0) CategoryBadge("Music ${result.musicCount}")
                if (result.documentsCount > 0) CategoryBadge("Docs ${result.documentsCount}")
                if (result.zipsCount > 0) CategoryBadge("ZIPs ${result.zipsCount}")
                if (result.imagesCount > 0) CategoryBadge("Images ${result.imagesCount}")
                if (result.otherCount > 0) CategoryBadge("Other ${result.otherCount}")
            }
            if (result.spaceFreedBytes > 0) {
                val sizeText = when {
                    result.spaceFreedBytes >= 1_048_576L -> "%.1f MB".format(result.spaceFreedBytes / 1_048_576.0)
                    result.spaceFreedBytes >= 1024L -> "%.1f KB".format(result.spaceFreedBytes / 1024.0)
                    else -> "${result.spaceFreedBytes} B"
                }
                Text("Space freed: $sizeText", color = TDTextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Remove Scan", color = TDTextPrimary) },
            text = { Text("Remove this scan from history?", color = TDTextSecondary) },
            confirmButton = { TextButton({ onDelete(); showDelete = false }) { Text("Remove", color = TDError) } },
            dismissButton = { TextButton({ showDelete = false }) { Text("Cancel", color = TDTextMuted) } },
            containerColor = TDSurface
        )
    }
}
