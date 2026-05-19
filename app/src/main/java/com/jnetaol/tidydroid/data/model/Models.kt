package com.jnetaol.tidydroid.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sort_rules")
data class SortRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "pattern") val pattern: String = "",
    @ColumnInfo(name = "category") val category: String = "Other",
    @ColumnInfo(name = "enabled") val enabled: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "files_found") val filesFound: Int = 0,
    @ColumnInfo(name = "files_moved") val filesMoved: Int = 0,
    @ColumnInfo(name = "videos_count") val videosCount: Int = 0,
    @ColumnInfo(name = "apks_count") val apksCount: Int = 0,
    @ColumnInfo(name = "music_count") val musicCount: Int = 0,
    @ColumnInfo(name = "documents_count") val documentsCount: Int = 0,
    @ColumnInfo(name = "zips_count") val zipsCount: Int = 0,
    @ColumnInfo(name = "images_count") val imagesCount: Int = 0,
    @ColumnInfo(name = "other_count") val otherCount: Int = 0,
    @ColumnInfo(name = "space_freed_bytes") val spaceFreedBytes: Long = 0L
)

@Entity(tableName = "file_duplicates")
data class FileDuplicate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "file_path") val filePath: String = "",
    @ColumnInfo(name = "file_name") val fileName: String = "",
    @ColumnInfo(name = "file_size") val fileSize: Long = 0,
    @ColumnInfo(name = "hash") val hash: String = "",
    @ColumnInfo(name = "category") val category: String = "Other",
    @ColumnInfo(name = "is_original") val isOriginal: Boolean = true,
    @ColumnInfo(name = "scan_timestamp") val scanTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "large_files")
data class LargeFileEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "file_path") val filePath: String = "",
    @ColumnInfo(name = "file_name") val fileName: String = "",
    @ColumnInfo(name = "file_size") val fileSize: Long = 0,
    @ColumnInfo(name = "category") val category: String = "Other",
    @ColumnInfo(name = "scan_timestamp") val scanTimestamp: Long = System.currentTimeMillis()
)
