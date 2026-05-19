package com.jnetaol.tidydroid.ui.screens.duplicates

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.jnetaol.tidydroid.data.model.FileDuplicate
import com.jnetaol.tidydroid.ui.components.*
import com.jnetaol.tidydroid.ui.screens.AppViewModel
import com.jnetaol.tidydroid.ui.theme.*

@Composable
fun DuplicatesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val duplicates by viewModel.duplicates.collectAsState()
    val isScanning by viewModel.isDuplicatingScanning.collectAsState()
    val wastedSpace = viewModel.formatSizeStr(viewModel.wastedSpace.value)
    val duplicateCount = duplicates.count { !it.isOriginal }

    val grouped = remember(duplicates) {
        duplicates.groupBy { it.hash }.filter { it.value.size > 1 }
    }

    Column(Modifier.fillMaxSize().background(TDBackground)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp).statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.Default.ArrowBack, null, tint = TDTextPrimary) }
            Text("Duplicates", color = TDTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        NeonCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("$duplicateCount duplicates", color = TDNeonOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("$wastedSpace can be freed", color = TDTextSecondary, fontSize = 13.sp)
                }
                GlowButton(
                    if (isScanning) "Scanning\u2026" else "Scan",
                    Icons.Default.Search, glowColor = TDNeonOrange,
                    enabled = !isScanning
                ) { viewModel.scanDuplicates() }
            }
            if (isScanning) {
                LinearProgressIndicator(Modifier.fillMaxWidth().height(3.dp).padding(bottom = 8.dp), color = TDNeonOrange, trackColor = TDSurfaceVariant)
            }
        }

        if (grouped.isEmpty() && !isScanning) {
            EmptyState(Icons.Default.ContentCopy, "No duplicates found", "Scan your downloads for duplicate files")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                grouped.forEach { (hash, group) ->
                    val originals = group.filter { it.isOriginal }
                    val dupes = group.filter { !it.isOriginal }
                    item(key = hash) {
                        DuplicateGroupCard(
                            original = originals.firstOrNull() ?: dupes.firstOrNull() ?: return@item,
                            duplicates = dupes,
                            totalDupes = dupes.size,
                            wastedForGroup = dupes.sumOf { it.fileSize },
                            formatSize = { viewModel.formatSizeStr(it) },
                            onDelete = { viewModel.deleteDuplicateFile(it) },
                            onDeleteAll = { viewModel.deleteDuplicateGroup(group) }
                        )
                    }
                }
                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }
}

@Composable
fun DuplicateGroupCard(
    original: FileDuplicate,
    duplicates: List<FileDuplicate>,
    totalDupes: Int,
    wastedForGroup: Long,
    formatSize: (Long) -> String,
    onDelete: (FileDuplicate) -> Unit,
    onDeleteAll: () -> Unit
) {
    var showDeleteAll by remember { mutableStateOf(false) }

    NeonCard(borderColor = TDNeonOrange.copy(alpha = 0.3f)) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${totalDupes} duplicate${if (totalDupes > 1) "s" else ""}", color = TDTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${formatSize(wastedForGroup)} wasted", color = TDNeonOrange, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.InsertDriveFile, null, Modifier.size(16.dp), tint = TDNeonGreen)
                Spacer(Modifier.width(8.dp))
                Text(original.fileName, color = TDNeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                StatusBadge("Original", TDNeonGreen, Modifier.padding(start = 8.dp))
            }
            Spacer(Modifier.height(6.dp))
            duplicates.forEach { dup ->
                var showDeleteOne by remember { mutableStateOf(false) }
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCopy, null, Modifier.size(14.dp), tint = TDTextMuted)
                    Spacer(Modifier.width(6.dp))
                    Text(dup.fileName, color = TDTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(formatSize(dup.fileSize), color = TDTextMuted, fontSize = 11.sp)
                    IconButton({ showDeleteOne = true }, Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = TDError, modifier = Modifier.size(16.dp))
                    }
                }
                if (showDeleteOne) {
                    AlertDialog(
                        onDismissRequest = { showDeleteOne = false },
                        title = { Text("Delete File", color = TDTextPrimary) },
                        text = { Text("Delete \"${dup.fileName}\"?", color = TDTextSecondary) },
                        confirmButton = { TextButton({ onDelete(dup); showDeleteOne = false }) { Text("Delete", color = TDError) } },
                        dismissButton = { TextButton({ showDeleteOne = false }) { Text("Cancel", color = TDTextMuted) } },
                        containerColor = TDSurface
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            GlowButton("Delete All Copies", Icons.Default.DeleteSweep, glowColor = TDError, modifier = Modifier.fillMaxWidth().height(36.dp)) { showDeleteAll = true }

            if (showDeleteAll) {
                AlertDialog(
                    onDismissRequest = { showDeleteAll = false },
                    title = { Text("Delete All Copies", color = TDTextPrimary) },
                    text = { Text("Delete all $totalDupes duplicate copies? Original will be kept.", color = TDTextSecondary) },
                    confirmButton = { TextButton({ onDeleteAll(); showDeleteAll = false }) { Text("Delete All", color = TDError) } },
                    dismissButton = { TextButton({ showDeleteAll = false }) { Text("Cancel", color = TDTextMuted) } },
                    containerColor = TDSurface
                )
            }
        }
    }
}
