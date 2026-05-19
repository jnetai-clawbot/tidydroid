package com.jnetaol.tidydroid.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.tidydroid.ui.components.*
import com.jnetaol.tidydroid.ui.screens.AppViewModel
import com.jnetaol.tidydroid.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(Modifier.fillMaxSize().background(TDBackground)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp).statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.Default.ArrowBack, null, tint = TDTextPrimary) }
            Text("Settings", color = TDTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NeonCard {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(48.dp), tint = TDPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("TidyDroid", color = TDTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Smart Download Organiser", color = TDTextMuted, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Version ${viewModel.appVersion}", color = TDPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("About", color = TDTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "TidyDroid automatically organizes your Downloads folder into categories like Videos, APKs, Music, Documents, ZIPs, and Images. Features include a custom rules engine, duplicate file finder, and large file cleaner with scan history and stats.",
                            color = TDTextSecondary, fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text("Made By ", color = TDTextSecondary, fontSize = 14.sp)
                            Text("jnetaol.com", color = TDPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, null, Modifier.size(14.dp), tint = TDSecondary)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                viewModel.aboutUrl,
                                color = TDSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(viewModel.aboutUrl))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Updates", color = TDTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Current Version", color = TDTextMuted, fontSize = 12.sp)
                                Text("v${viewModel.appVersion}", color = TDTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                            StatusBadge("Up to date", TDSuccess)
                        }
                        Spacer(Modifier.height(12.dp))
                        GlowButton(
                            "Check For Updates",
                            Icons.Default.SystemUpdateAlt,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(viewModel.githubReleasesUrl))
                                context.startActivity(intent)
                            },
                            glowColor = TDSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Share", color = TDTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlowButton(
                                "Share App",
                                Icons.Default.Share,
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "TidyDroid - Smart Download Organiser")
                                        putExtra(Intent.EXTRA_TEXT, viewModel.shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share TidyDroid"))
                                },
                                glowColor = TDPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            GlowButton(
                                "Copy Link",
                                Icons.Default.ContentCopy,
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(viewModel.githubReleasesUrl))
                                    viewModel.showToast("Link copied!")
                                },
                                glowColor = TDSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Features", color = TDTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        FeatureRow("Automatic downloads folder organization")
                        FeatureRow("Custom sorting rules engine")
                        FeatureRow("Duplicate file finder")
                        FeatureRow("Large file cleaner")
                        FeatureRow("Scan history with category stats")
                        FeatureRow("Dark Material Design 3 with neon theme")
                    }
                }
            }

            item {
                NeonCard(borderColor = TDError.copy(alpha = 0.3f)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Data Management", color = TDTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Text("Clear all scan history, duplicate results, and large file lists. This does not delete actual files from your device.", color = TDTextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(12.dp))
                        GlowButton("Clear All Data", Icons.Default.DeleteSweep, onClick = { viewModel.clearAllData() }, glowColor = TDError, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            item {
                NeonCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Legal", color = TDTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("MIT License", color = TDTextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Copyright (c) 2024 jnetaol.com", color = TDTextMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "This software is provided \"as-is\" without warranty. Use responsibly and review files before deletion.",
                            color = TDTextMuted, fontSize = 12.sp
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun FeatureRow(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = TDSuccess)
        Spacer(Modifier.width(8.dp))
        Text(text, color = TDTextSecondary, fontSize = 13.sp)
    }
}
