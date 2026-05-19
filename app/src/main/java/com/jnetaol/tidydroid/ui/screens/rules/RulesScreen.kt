package com.jnetaol.tidydroid.ui.screens.rules

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.tidydroid.data.model.SortRule
import com.jnetaol.tidydroid.ui.components.*
import com.jnetaol.tidydroid.ui.screens.AppViewModel
import androidx.compose.ui.draw.alpha
import com.jnetaol.tidydroid.ui.theme.*

private val categories = listOf("Videos", "APKs", "Music", "Documents", "ZIPs", "Images", "Other")

@Composable
fun RulesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val rules by viewModel.rules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<SortRule?>(null) }

    Column(Modifier.fillMaxSize().background(TDBackground)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp).statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.Default.ArrowBack, null, tint = TDTextPrimary) }
            Text("Sorting Rules", color = TDTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            GlowButton("Add", Icons.Default.Add, glowColor = TDPrimary, modifier = Modifier.height(44.dp), onClick = { showAddDialog = true })
        }

        if (rules.isEmpty()) {
            EmptyState(Icons.Default.Rule, "No custom rules", "Add rules to customize file sorting")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rules, key = { it.id }) { rule ->
                    RuleCard(
                        rule = rule,
                        onToggle = { viewModel.toggleRuleEnabled(rule.id, !rule.enabled) },
                        onEdit = { editingRule = rule },
                        onDelete = { viewModel.deleteRule(rule) }
                    )
                }
                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }

    if (showAddDialog) {
        RuleDialog(
            rule = null,
            onDismiss = { showAddDialog = false },
            onSave = { pattern, category ->
                viewModel.saveRule(SortRule(pattern = pattern, category = category))
                showAddDialog = false
            }
        )
    }

    if (editingRule != null) {
        val rule = editingRule!!
        RuleDialog(
            rule = rule,
            onDismiss = { editingRule = null },
            onSave = { pattern, category ->
                viewModel.saveRule(rule.copy(pattern = pattern, category = category))
                editingRule = null
            }
        )
    }
}

@Composable
fun RuleCard(
    rule: SortRule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }
    val alpha = if (rule.enabled) 1f else 0.5f

    NeonCard {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = rule.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = TDPrimary, checkedTrackColor = TDPrimary.copy(alpha = 0.3f))
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f).alpha(alpha)) {
                Text(rule.pattern, color = TDTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Row {
                    CategoryBadge(rule.category)
                    Spacer(Modifier.width(6.dp))
                    if (!rule.enabled) StatusBadge("Disabled", TDTextMuted)
                }
            }
            IconButton(onEdit, Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, null, tint = TDTextMuted, modifier = Modifier.size(18.dp))
            }
            IconButton({ showDelete = true }, Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, null, tint = TDError, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete Rule", color = TDTextPrimary) },
            text = { Text("Delete rule \"${rule.pattern}\"?", color = TDTextSecondary) },
            confirmButton = { TextButton({ onDelete(); showDelete = false }) { Text("Delete", color = TDError) } },
            dismissButton = { TextButton({ showDelete = false }) { Text("Cancel", color = TDTextMuted) } },
            containerColor = TDSurface
        )
    }
}

@Composable
fun RuleDialog(
    rule: SortRule?,
    onDismiss: () -> Unit,
    onSave: (pattern: String, category: String) -> Unit
) {
    var pattern by remember { mutableStateOf(rule?.pattern ?: "") }
    var selectedCategory by remember { mutableStateOf(rule?.category ?: "Other") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (rule != null) "Edit Rule" else "Add Rule", color = TDPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text("Pattern (regex)", color = TDTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    NeonTextField(pattern, { pattern = it }, "e.g. .*\\.torrent$", leadingIcon = Icons.Default.Code)
                }
                Column {
                    Text("Category", color = TDTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.Folder, null, tint = TDTextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TDTextPrimary, unfocusedTextColor = TDTextPrimary,
                                focusedBorderColor = TDPrimary.copy(alpha = 0.5f), unfocusedBorderColor = TDSurfaceVariant,
                                cursorColor = TDPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Box(Modifier.matchParentSize().clickable { expanded = !expanded })
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = TDTextPrimary) },
                                    onClick = { selectedCategory = cat; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            GlowButton("Save", Icons.Default.Save, glowColor = TDPrimary, enabled = pattern.isNotBlank(), onClick = {
                onSave(pattern, selectedCategory)
            })
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel", color = TDTextMuted) } },
        containerColor = TDSurface
    )
}
