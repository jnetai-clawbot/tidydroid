package com.jnetaol.tidydroid.engine

import android.content.Context
import android.os.Environment
import com.jnetaol.tidydroid.data.model.*
import com.jnetaol.tidydroid.logger.DebugLogger
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class FileOrganizer(private val context: Context) {

    private val downloadsDir: File
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    private val defaultPatterns = mapOf(
        "Videos" to listOf(
            ".*\\.mp4$", ".*\\.mkv$", ".*\\.avi$", ".*\\.mov$", ".*\\.wmv$",
            ".*\\.flv$", ".*\\.webm$", ".*\\.3gp$", ".*\\.m4v$", ".*\\.ts$"
        ),
        "APKs" to listOf(".*\\.apk$", ".*\\.xapk$", ".*\\.apkm$", ".*\\.aab$"),
        "Music" to listOf(
            ".*\\.mp3$", ".*\\.wav$", ".*\\.flac$", ".*\\.aac$", ".*\\.ogg$",
            ".*\\.wma$", ".*\\.m4a$", ".*\\.opus$", ".*\\.midi?$"
        ),
        "Documents" to listOf(
            ".*\\.pdf$", ".*\\.docx?$", ".*\\.xlsx?$", ".*\\.pptx?$",
            ".*\\.txt$", ".*\\.csv$", ".*\\.rtf$", ".*\\.odt$", ".*\\.ods$",
            ".*\\.odp$", ".*\\.epub$", ".*\\.mobi$", ".*\\.log$", ".*\\.md$"
        ),
        "ZIPs" to listOf(
            ".*\\.zip$", ".*\\.rar$", ".*\\.7z$", ".*\\.tar$", ".*\\.gz$",
            ".*\\.bz2$", ".*\\.xz$", ".*\\.tgz$", ".*\\.iso$"
        ),
        "Images" to listOf(
            ".*\\.jpg$", ".*\\.jpeg$", ".*\\.png$", ".*\\.gif$", ".*\\.bmp$",
            ".*\\.webp$", ".*\\.svg$", ".*\\.ico$", ".*\\.heic$", ".*\\.avif$"
        )
    )

    data class OrganizeResult(
        val filesFound: Int,
        val filesMoved: Int,
        val videosCount: Int,
        val apksCount: Int,
        val musicCount: Int,
        val documentsCount: Int,
        val zipsCount: Int,
        val imagesCount: Int,
        val otherCount: Int,
        val spaceFreedBytes: Long
    )

    data class DuplicateFindResult(
        val duplicates: List<FileDuplicate>,
        val wastedSpace: Long
    )

    data class LargeFilesResult(
        val largeFiles: List<LargeFileEntry>,
        val totalSize: Long
    )

    fun scanAndOrganize(rules: List<SortRule>): OrganizeResult {
        DebugLogger.i("FileOrganizer", "Starting scan and organize", "TD-FO-001")
        if (!downloadsDir.exists() || !downloadsDir.isDirectory) {
            DebugLogger.w("FileOrganizer", "Downloads dir not found", "TD-FO-WARN-001")
            return OrganizeResult(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        }

        val categoryPatterns = mutableMapOf<String, List<String>>()
        categoryPatterns.putAll(defaultPatterns)

        for (rule in rules) {
            if (!rule.enabled) continue
            val patterns = categoryPatterns.getOrPut(rule.category) { emptyList() }
            categoryPatterns[rule.category] = patterns + listOf(rule.pattern)
        }

        var videosCount = 0; var apksCount = 0; var musicCount = 0
        var documentsCount = 0; var zipsCount = 0; var imagesCount = 0; var otherCount = 0
        var filesMoved = 0; var spaceFreed = 0L

        val files = downloadsDir.listFiles()?.filter { it.isFile } ?: emptyList()
        val filesFound = files.size

        files.forEach { file ->
            val category = categorizeFile(file.name, categoryPatterns)
            when (category) {
                "Videos" -> videosCount++
                "APKs" -> apksCount++
                "Music" -> musicCount++
                "Documents" -> documentsCount++
                "ZIPs" -> zipsCount++
                "Images" -> imagesCount++
                else -> otherCount++
            }

            if (category != "Other") {
                val targetDir = File(downloadsDir, category)
                try {
                    if (!targetDir.exists()) targetDir.mkdirs()
                    val targetFile = File(targetDir, file.name)
                    var finalTarget = targetFile
                    var counter = 1
                    while (finalTarget.exists()) {
                        val nameWithoutExt = file.nameWithoutExtension
                        val ext = file.extension
                        finalTarget = File(targetDir, "${nameWithoutExt}_${counter}.${ext}")
                        counter++
                    }
                    if (file.renameTo(finalTarget)) {
                        filesMoved++
                        spaceFreed += file.length()
                    }
                } catch (e: Exception) {
                    DebugLogger.e("FileOrganizer", "Failed to move: ${file.name}", "TD-FO-ERR-001", e)
                }
            }
        }

        DebugLogger.i("FileOrganizer", "Organized: $filesMoved/$filesFound files, $spaceFreed bytes", "TD-FO-002")
        return OrganizeResult(
            filesFound, filesMoved, videosCount, apksCount, musicCount,
            documentsCount, zipsCount, imagesCount, otherCount, spaceFreed
        )
    }

    fun findDuplicates(): DuplicateFindResult {
        DebugLogger.i("FileOrganizer", "Starting duplicate scan", "TD-FO-003")
        if (!downloadsDir.exists() || !downloadsDir.isDirectory) {
            return DuplicateFindResult(emptyList(), 0)
        }

        val files = downloadsDir.listFiles()?.filter { it.isFile } ?: emptyList()
        val sizeMap = mutableMapOf<Long, MutableList<File>>()
        files.forEach { file ->
            sizeMap.getOrPut(file.length()) { mutableListOf() }.add(file)
        }

        val scanTime = System.currentTimeMillis()
        val duplicates = mutableListOf<FileDuplicate>()
        var wastedSpace = 0L

        sizeMap.filter { it.value.size > 1 }.forEach { (_, sameSizeFiles) ->
            val hashMap = mutableMapOf<String, MutableList<File>>()
            sameSizeFiles.forEach { file ->
                val hash = computeFileHash(file)
                if (hash.isNotEmpty()) {
                    hashMap.getOrPut(hash) { mutableListOf() }.add(file)
                }
            }
            hashMap.filter { it.value.size > 1 }.forEach { (hash, group) ->
                group.sortedByDescending { it.lastModified() }.forEachIndexed { index, file ->
                    duplicates.add(FileDuplicate(
                        filePath = file.absolutePath,
                        fileName = file.name,
                        fileSize = file.length(),
                        hash = hash,
                        category = categorizeFile(file.name, defaultPatterns),
                        isOriginal = index == 0,
                        scanTimestamp = scanTime
                    ))
                    if (index > 0) wastedSpace += file.length()
                }
            }
        }

        DebugLogger.i("FileOrganizer", "Found ${duplicates.size} duplicates, $wastedSpace wasted", "TD-FO-004")
        return DuplicateFindResult(duplicates, wastedSpace)
    }

    fun findLargeFiles(minSizeBytes: Long = 50L * 1024 * 1024): LargeFilesResult {
        DebugLogger.i("FileOrganizer", "Starting large file scan (min: $minSizeBytes)", "TD-FO-005")
        if (!downloadsDir.exists() || !downloadsDir.isDirectory) {
            return LargeFilesResult(emptyList(), 0)
        }

        val scanTime = System.currentTimeMillis()
        val largeFiles = downloadsDir.listFiles()
            ?.filter { it.isFile && it.length() >= minSizeBytes }
            ?.sortedByDescending { it.length() }
            ?.map { file ->
                LargeFileEntry(
                    filePath = file.absolutePath,
                    fileName = file.name,
                    fileSize = file.length(),
                    category = categorizeFile(file.name, defaultPatterns),
                    scanTimestamp = scanTime
                )
            } ?: emptyList()

        val totalSize = largeFiles.sumOf { it.fileSize }
        DebugLogger.i("FileOrganizer", "Found ${largeFiles.size} large files, $totalSize total", "TD-FO-006")
        return LargeFilesResult(largeFiles, totalSize)
    }

    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return try {
            if (file.exists() && file.isFile && path.startsWith(downloadsDir.absolutePath)) {
                val deleted = file.delete()
                if (deleted) DebugLogger.i("FileOrganizer", "Deleted: $path", "TD-FO-007")
                deleted
            } else false
        } catch (e: Exception) {
            DebugLogger.e("FileOrganizer", "Delete failed: $path", "TD-FO-ERR-002", e)
            false
        }
    }

    fun deleteAllDuplicates(duplicates: List<FileDuplicate>, keepOriginal: Boolean = true): Int {
        var deleted = 0
        duplicates.forEach { dup ->
            if (keepOriginal && dup.isOriginal) return@forEach
            if (deleteFile(dup.filePath)) deleted++
        }
        DebugLogger.i("FileOrganizer", "Deleted $deleted duplicates", "TD-FO-008")
        return deleted
    }

    private fun categorizeFile(fileName: String, patterns: Map<String, List<String>>): String {
        val name = fileName.lowercase(Locale.ROOT)
        for ((category, patternList) in patterns) {
            for (rawPattern in patternList) {
                val pattern = rawPattern.removePrefix(".*").removeSuffix("$")
                if (name.endsWith(pattern.trimStart('.'))) return category
            }
        }
        return "Other"
    }

    private fun computeFileHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                val maxRead = 1024L * 1024
                var totalRead = 0L
                while (fis.read(buffer).also { bytesRead = it } != -1 && totalRead < maxRead) {
                    val toRead = minOf(bytesRead, (maxRead - totalRead).toInt())
                    digest.update(buffer, 0, toRead)
                    totalRead += toRead
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }
}
