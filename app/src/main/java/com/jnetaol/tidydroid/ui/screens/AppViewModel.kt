package com.jnetaol.tidydroid.ui.screens

import android.app.Application
import android.os.Build
import android.os.Environment
import androidx.lifecycle.*
import com.jnetaol.tidydroid.data.db.AppDatabase
import com.jnetaol.tidydroid.data.model.*
import com.jnetaol.tidydroid.engine.FileOrganizer
import com.jnetaol.tidydroid.logger.DebugLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val organizer = FileOrganizer(application)

    private val _rules = MutableStateFlow<List<SortRule>>(emptyList())
    val rules: StateFlow<List<SortRule>> = _rules.asStateFlow()

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults.asStateFlow()

    private val _duplicates = MutableStateFlow<List<FileDuplicate>>(emptyList())
    val duplicates: StateFlow<List<FileDuplicate>> = _duplicates.asStateFlow()

    private val _largeFiles = MutableStateFlow<List<LargeFileEntry>>(emptyList())
    val largeFiles: StateFlow<List<LargeFileEntry>> = _largeFiles.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isDuplicatingScanning = MutableStateFlow(false)
    val isDuplicatingScanning: StateFlow<Boolean> = _isDuplicatingScanning.asStateFlow()

    private val _isLargeFilesScanning = MutableStateFlow(false)
    val isLargeFilesScanning: StateFlow<Boolean> = _isLargeFilesScanning.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _totalOrganized = MutableStateFlow(0)
    val totalOrganized: StateFlow<Int> = _totalOrganized.asStateFlow()

    private val _totalSpaceFreed = MutableStateFlow(0L)
    val totalSpaceFreed: StateFlow<Long> = _totalSpaceFreed.asStateFlow()

    private val _totalDuplicatesFound = MutableStateFlow(0)
    val totalDuplicatesFound: StateFlow<Int> = _totalDuplicatesFound.asStateFlow()

    private val _wastedSpace = MutableStateFlow(0L)
    val wastedSpace: StateFlow<Long> = _wastedSpace.asStateFlow()

    private val _largeFilesCount = MutableStateFlow(0)
    val largeFilesCount: StateFlow<Int> = _largeFilesCount.asStateFlow()

    private val _largeFilesTotalSize = MutableStateFlow(0L)
    val largeFilesTotalSize: StateFlow<Long> = _largeFilesTotalSize.asStateFlow()

    private val _needsStoragePermission = MutableStateFlow(false)
    val needsStoragePermission: StateFlow<Boolean> = _needsStoragePermission.asStateFlow()

    init {
        DebugLogger.d("AppViewModel", "ViewModel init", "TD-VM-001")
        loadAll()
    }

    fun loadAll() {
        loadRules()
        loadScanResults()
        loadDuplicates()
        loadLargeFiles()
        loadStats()
        checkStoragePermission()
    }

    fun checkStoragePermission() {
        _needsStoragePermission.value = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !Environment.isExternalStorageManager()
    }

    private fun loadRules() {
        scope.launch {
            try { _rules.value = db.rulesDao().getAll() }
            catch (e: Exception) { DebugLogger.e("AppViewModel", "Load rules failed", "TD-VM-ERR-001", e) }
        }
    }

    private fun loadScanResults() {
        scope.launch {
            try { _scanResults.value = db.scanResultsDao().getRecent() }
            catch (e: Exception) { DebugLogger.e("AppViewModel", "Load scans failed", "TD-VM-ERR-002", e) }
        }
    }

    private fun loadDuplicates() {
        scope.launch {
            try { _duplicates.value = db.fileDuplicatesDao().getAll() }
            catch (e: Exception) { DebugLogger.e("AppViewModel", "Load duplicates failed", "TD-VM-ERR-003", e) }
        }
    }

    private fun loadLargeFiles() {
        scope.launch {
            try { _largeFiles.value = db.largeFilesDao().getAll() }
            catch (e: Exception) { DebugLogger.e("AppViewModel", "Load large files failed", "TD-VM-ERR-004", e) }
        }
    }

    private fun loadStats() {
        scope.launch {
            try {
                _totalOrganized.value = db.scanResultsDao().getTotalOrganized()
                _totalSpaceFreed.value = db.scanResultsDao().getTotalSpaceFreed()
                _totalDuplicatesFound.value = db.fileDuplicatesDao().getDuplicateCount()
                _wastedSpace.value = db.fileDuplicatesDao().getWastedSpace()
                _largeFilesCount.value = db.largeFilesDao().getCount()
                _largeFilesTotalSize.value = db.largeFilesDao().getTotalSize()
            } catch (e: Exception) { DebugLogger.e("AppViewModel", "Load stats failed", "TD-VM-ERR-005", e) }
        }
    }

    fun organizeNow() {
        if (_isScanning.value) return
        _isScanning.value = true
        scope.launch {
            try {
                DebugLogger.i("AppViewModel", "Organize started", "TD-VM-002")
                val enabledRules = db.rulesDao().getEnabled()
                val result = withContext(Dispatchers.IO) { organizer.scanAndOrganize(enabledRules) }

                val scanResult = ScanResult(
                    timestamp = System.currentTimeMillis(),
                    filesFound = result.filesFound,
                    filesMoved = result.filesMoved,
                    videosCount = result.videosCount,
                    apksCount = result.apksCount,
                    musicCount = result.musicCount,
                    documentsCount = result.documentsCount,
                    zipsCount = result.zipsCount,
                    imagesCount = result.imagesCount,
                    otherCount = result.otherCount,
                    spaceFreedBytes = result.spaceFreedBytes
                )
                db.scanResultsDao().insert(scanResult)

                showToast("${result.filesMoved} files organized into categories")
                DebugLogger.i("AppViewModel", "Organize complete: ${result.filesMoved} files", "TD-VM-003")
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Organize failed", "TD-VM-ERR-006", e)
                showToast("Organization failed: ${e.message}")
            } finally {
                _isScanning.value = false
                loadAll()
            }
        }
    }

    fun scanDuplicates() {
        if (_isDuplicatingScanning.value) return
        _isDuplicatingScanning.value = true
        scope.launch {
            try {
                DebugLogger.i("AppViewModel", "Duplicate scan started", "TD-VM-004")
                db.fileDuplicatesDao().deleteAll()
                val result = withContext(Dispatchers.IO) { organizer.findDuplicates() }
                if (result.duplicates.isNotEmpty()) {
                    db.fileDuplicatesDao().insertAll(result.duplicates)
                }
                showToast("${result.duplicates.count { !it.isOriginal }} duplicates found")
                DebugLogger.i("AppViewModel", "Duplicate scan complete", "TD-VM-005")
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Duplicate scan failed", "TD-VM-ERR-007", e)
                showToast("Duplicate scan failed: ${e.message}")
            } finally {
                _isDuplicatingScanning.value = false
                loadAll()
            }
        }
    }

    fun scanLargeFiles(minSizeMB: Int = 50) {
        if (_isLargeFilesScanning.value) return
        _isLargeFilesScanning.value = true
        scope.launch {
            try {
                DebugLogger.i("AppViewModel", "Large file scan started (min: ${minSizeMB}MB)", "TD-VM-006")
                db.largeFilesDao().deleteAll()
                val result = withContext(Dispatchers.IO) {
                    organizer.findLargeFiles(minSizeMB.toLong() * 1024 * 1024)
                }
                if (result.largeFiles.isNotEmpty()) {
                    db.largeFilesDao().insertAll(result.largeFiles)
                }
                showToast("${result.largeFiles.size} large files found")
                DebugLogger.i("AppViewModel", "Large file scan complete", "TD-VM-007")
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Large file scan failed", "TD-VM-ERR-008", e)
                showToast("Large file scan failed: ${e.message}")
            } finally {
                _isLargeFilesScanning.value = false
                loadAll()
            }
        }
    }

    fun deleteDuplicateFile(duplicate: FileDuplicate) {
        scope.launch {
            try {
                if (organizer.deleteFile(duplicate.filePath)) {
                    db.fileDuplicatesDao().deleteById(duplicate.id)
                    showToast("File deleted")
                    DebugLogger.i("AppViewModel", "Duplicate deleted: ${duplicate.fileName}", "TD-VM-008")
                } else {
                    showToast("Delete failed")
                }
                loadAll()
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Delete duplicate failed", "TD-VM-ERR-009", e)
                showToast("Delete failed: ${e.message}")
            }
        }
    }

    fun deleteDuplicateGroup(duplicates: List<FileDuplicate>) {
        scope.launch {
            try {
                var deleted = 0
                duplicates.forEach { dup ->
                    if (!dup.isOriginal && organizer.deleteFile(dup.filePath)) {
                        db.fileDuplicatesDao().deleteById(dup.id)
                        deleted++
                    }
                }
                showToast("$deleted duplicates deleted")
                loadAll()
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Delete duplicates failed", "TD-VM-ERR-010", e)
            }
        }
    }

    fun deleteLargeFile(file: LargeFileEntry) {
        scope.launch {
            try {
                if (organizer.deleteFile(file.filePath)) {
                    db.largeFilesDao().deleteById(file.id)
                    showToast("File deleted")
                } else {
                    showToast("Delete failed")
                }
                loadAll()
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Delete large file failed", "TD-VM-ERR-011", e)
                showToast("Delete failed: ${e.message}")
            }
        }
    }

    fun saveRule(rule: SortRule) {
        scope.launch {
            try {
                if (rule.id == 0L) db.rulesDao().insert(rule)
                else db.rulesDao().update(rule)
                showToast("Rule saved")
                DebugLogger.i("AppViewModel", "Rule saved: ${rule.pattern}", "TD-VM-009")
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Save rule failed", "TD-VM-ERR-012", e)
            }
            loadRules()
        }
    }

    fun deleteRule(rule: SortRule) {
        scope.launch {
            try {
                db.rulesDao().delete(rule.id)
                showToast("Rule deleted")
                DebugLogger.i("AppViewModel", "Rule deleted: ${rule.pattern}", "TD-VM-010")
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Delete rule failed", "TD-VM-ERR-013", e)
            }
            loadRules()
        }
    }

    fun toggleRuleEnabled(id: Long, enabled: Boolean) {
        scope.launch {
            try {
                db.rulesDao().toggleEnabled(id, enabled)
                loadRules()
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Toggle rule failed", "TD-VM-ERR-014", e)
            }
        }
    }

    fun deleteScanResult(result: ScanResult) {
        scope.launch {
            try {
                db.scanResultsDao().delete(result.id)
                loadAll()
                showToast("Scan history removed")
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Delete scan failed", "TD-VM-ERR-015", e)
            }
        }
    }

    fun clearAllData() {
        scope.launch {
            try {
                db.scanResultsDao().deleteAll()
                db.fileDuplicatesDao().deleteAll()
                db.largeFilesDao().deleteAll()
                showToast("All scan data cleared")
                DebugLogger.i("AppViewModel", "All data cleared", "TD-VM-011")
            } catch (e: Exception) {
                DebugLogger.e("AppViewModel", "Clear data failed", "TD-VM-ERR-016", e)
            }
            loadAll()
        }
    }

    fun showToast(msg: String) { scope.launch { _toastMessage.emit(msg) } }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    fun formatSizeStr(bytes: Long): String = formatSize(bytes)

    val statsFormatted: Map<String, String>
        get() = mapOf(
            "organized" to "$totalOrganized.value",
            "scans" to "${_scanResults.value.size}",
            "spaceFreed" to formatSize(_totalSpaceFreed.value),
            "duplicates" to "${_totalDuplicatesFound.value}",
            "wasted" to formatSize(_wastedSpace.value),
            "largeFiles" to "${_largeFilesCount.value}",
            "largeSize" to formatSize(_largeFilesTotalSize.value)
        )

    val appVersion: String get() = "1.0.0"
    val githubReleasesUrl: String get() = "https://github.com/jnetaol/TidyDroid/releases"
    val aboutUrl: String get() = "https://jnetaol.com"
    val shareText: String get() = "Organize your downloads automatically with TidyDroid - Smart Download Organiser. Sort files by category, find duplicates, clean large files, and customize with rules.\n\nDownload: $githubReleasesUrl"

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
        DebugLogger.d("AppViewModel", "Cleared", "TD-VM-012")
    }
}
