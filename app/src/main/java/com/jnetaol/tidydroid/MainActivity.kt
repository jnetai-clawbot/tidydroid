package com.jnetaol.tidydroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jnetaol.tidydroid.ui.screens.AppViewModel
import com.jnetaol.tidydroid.ui.screens.cleaner.CleanerScreen
import com.jnetaol.tidydroid.ui.screens.duplicates.DuplicatesScreen
import com.jnetaol.tidydroid.ui.screens.home.HomeScreen
import com.jnetaol.tidydroid.ui.screens.rules.RulesScreen
import com.jnetaol.tidydroid.ui.screens.settings.SettingsScreen
import com.jnetaol.tidydroid.ui.theme.*
import com.jnetaol.tidydroid.logger.DebugLogger
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DebugLogger.d("MainActivity", "onCreate", "TD-MA-001")
        setContent {
            TidyDroidTheme {
                val viewModel: AppViewModel = viewModel()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collect { message ->
                        snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                    }
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = TDSurface,
                                contentColor = TDTextPrimary
                            )
                        }
                    },
                    containerColor = TDBackground
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        when (currentScreen) {
                            Screen.Home -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToRules = { currentScreen = Screen.Rules },
                                onNavigateToDuplicates = { currentScreen = Screen.Duplicates },
                                onNavigateToCleaner = { currentScreen = Screen.Cleaner },
                                onNavigateToSettings = { currentScreen = Screen.Settings }
                            )
                            Screen.Rules -> RulesScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.Home }
                            )
                            Screen.Duplicates -> DuplicatesScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.Home }
                            )
                            Screen.Cleaner -> CleanerScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.Home }
                            )
                            Screen.Settings -> SettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.Home }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        DebugLogger.d("MainActivity", "onDestroy", "TD-MA-002")
        super.onDestroy()
    }

    private enum class Screen { Home, Rules, Duplicates, Cleaner, Settings }
}
