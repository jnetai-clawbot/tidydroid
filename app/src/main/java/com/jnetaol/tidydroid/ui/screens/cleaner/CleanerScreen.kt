package com.jnetaol.tidydroid.ui.screens.cleaner

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.tidydroid.data.model.LargeFileEntry
import com.jnetaol.tidydroid.ui.components.*
import com.jnetaol.tidydroid.ui.screens.AppViewModel
import com.jnetaol.tidydroid.ui.theme.*

@Composable
fun CleanerScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val largeFiles by viewModel.largeFiles.collectAsState()
    val isScanning by viewModel.isLargeFilesScanning.collectAsState()
    val largeFilesCount = viewModel.largeFilesCount.value
    val totalSize = viewModel.formatSizeStr(viewModel.largeFilesTotalSize.value)
    var minSizeMB by remember { mutableIntStateOf(50) }
    val sizeOptions = listOf(10, 25, 50, 100, 250, 500, 1000)

    Column(Modifier.fillMaxSize().background(TDBackground)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp).statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.Default.ArrowBack, null, tint = TDTextPrimary) }
            Text("Large File Cleaner", color = TDTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                NeonCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Min File Size", color = TDTextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            sizeOptions.forEach { size ->
                                FilterChip(
                                    selected = minSizeMB == size,
                                    onClick = { minSizeMB = size },
                                    label = { Text("${size}MB", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TDNeonRed.copy(alpha = 0.2f),
                                        selectedLabelColor = TDNeonRed
                                    ),
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("$largeFilesCount files", color = TDNeonRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("$totalSize total", color = TDTextSecondary, fontSize = 13.sp)
                            }
                            GlowButton(
                                if (isScanning) "Scanning\u2026" else "Scan",
                                Icons.Default.Search, glowColor = TDNeonRed,
                                enabled = !isScanning,
                                onClick = { viewModel.scanLargeFiles(minSizeMB) }
                            )
                        }
                    }
                    if (isScanning) {
                        LinearProgressIndicator(Modifier.fillMaxWidth().height(3.dp).padding(bottom = 4.dp), color = TDNeonRed, trackColor = TDSurfaceVariant)
                    }
                }
            }

            if (largeFiles.isEmpty() && !isScanning) {
                item { EmptyState(Icons.Default.DeleteSweep, "No large files found", "Scan for files larger than ${minSizeMB}MB in downloads") }
            } else {
                items(largeFiles, key = { it.id }) { file ->
                    LargeFileCard(
                        file = file,
                        onDelete = { viewModel.deleteLargeFile(file) },
                        formatSize = { viewModel.formatSizeStr(it) }
                    )
                }
                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }
}

@Composable
fun LargeFileCard(
    file: LargeFileEntry,
    onDelete: () -> Unit,
    formatSize: (Long) -> String
) {
    var showDelete by remember { mutableStateOf(false) }
    val sizeText = formatSize(file.fileSize)
    val categoryColor = when (file.category) {
        "Videos" -> TDNeonRed; "APKs" -> TDNeonGreen; "Music" -> TDNeonPurple
        "Documents" -> TDInfo; "ZIPs" -> TDNeonOrange; "Images" -> TDSecondary
        else -> TDTextMuted
    }

    NeonCard {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(categoryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (file.category) {
                        "Videos" -> Icons.Default.Videocam; "APKs" -> Icons.Default.Android
                        "Music" -> Icons.Default.MusicNote; "Documents" -> Icons.Default.Description
                        "ZIPs" -> Icons.Default.FolderZip; "Images" -> Icons.Default.Image
                        else -> Icons.Default.InsertDriveFile
                    },
                    null, tint = categoryColor, modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(file.fileName, color = TDTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sizeText, color = TDNeonRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    CategoryBadge(file.category)
                }
            }
            IconButton({ showDelete = true }, Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, null, tint = TDError, modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete File", color = TDTextPrimary) },
            text = { Text("Delete \"${file.fileName}\" ($sizeText)? This cannot be undone.", color = TDTextSecondary) },
            confirmButton = { TextButton({ onDelete(); showDelete = false }) { Text("Delete", color = TDError) } },
            dismissButton = { TextButton({ showDelete = false }) { Text("Cancel", color = TDTextMuted) } },
            containerColor = TDSurface
        )
    }
}
