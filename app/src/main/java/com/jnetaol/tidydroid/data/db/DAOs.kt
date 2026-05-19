package com.jnetaol.tidydroid.data.db

import androidx.room.*
import com.jnetaol.tidydroid.data.model.*

@Dao
interface RulesDao {
    @Query("SELECT * FROM sort_rules ORDER BY enabled DESC, created_at DESC")
    suspend fun getAll(): List<SortRule>

    @Query("SELECT * FROM sort_rules WHERE enabled = 1 ORDER BY created_at DESC")
    suspend fun getEnabled(): List<SortRule>

    @Query("SELECT * FROM sort_rules WHERE id = :id")
    suspend fun getById(id: Long): SortRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: SortRule): Long

    @Update
    suspend fun update(rule: SortRule)

    @Query("UPDATE sort_rules SET enabled = :enabled WHERE id = :id")
    suspend fun toggleEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM sort_rules WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM sort_rules")
    suspend fun deleteAll()
}

@Dao
interface ScanResultsDao {
    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    suspend fun getAll(): List<ScanResult>

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecent(): List<ScanResult>

    @Query("SELECT * FROM scan_results WHERE id = :id")
    suspend fun getById(id: Long): ScanResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scanResult: ScanResult): Long

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM scan_results")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM scan_results")
    suspend fun getCount(): Int

    @Query("SELECT COALESCE(SUM(files_moved), 0) FROM scan_results")
    suspend fun getTotalOrganized(): Int

    @Query("SELECT COALESCE(SUM(space_freed_bytes), 0) FROM scan_results")
    suspend fun getTotalSpaceFreed(): Long

    @Query("SELECT MAX(timestamp) FROM scan_results")
    suspend fun getLastScanTimestamp(): Long?
}

@Dao
interface FileDuplicatesDao {
    @Query("SELECT * FROM file_duplicates ORDER BY scan_timestamp DESC")
    suspend fun getAll(): List<FileDuplicate>

    @Query("SELECT * FROM file_duplicates WHERE scan_timestamp = :scanTime ORDER BY file_size DESC")
    suspend fun getByScanTime(scanTime: Long): List<FileDuplicate>

    @Query("SELECT * FROM file_duplicates WHERE hash = :hash AND id != :excludeId")
    suspend fun getDuplicatesOf(hash: String, excludeId: Long): List<FileDuplicate>

    @Query("SELECT DISTINCT hash FROM file_duplicates ORDER BY scan_timestamp DESC")
    suspend fun getDistinctHashes(): List<String>

    @Query("SELECT COUNT(DISTINCT hash) FROM file_duplicates WHERE is_original = 0")
    suspend fun getDuplicateCount(): Int

    @Query("SELECT COALESCE(SUM(file_size), 0) FROM file_duplicates WHERE is_original = 0")
    suspend fun getWastedSpace(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(duplicate: FileDuplicate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(duplicates: List<FileDuplicate>)

    @Query("DELETE FROM file_duplicates")
    suspend fun deleteAll()

    @Query("DELETE FROM file_duplicates WHERE scan_timestamp != :currentScan")
    suspend fun deleteOldScans(currentScan: Long)

    @Query("DELETE FROM file_duplicates WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface LargeFilesDao {
    @Query("SELECT * FROM large_files ORDER BY file_size DESC")
    suspend fun getAll(): List<LargeFileEntry>

    @Query("SELECT * FROM large_files WHERE scan_timestamp = :scanTime ORDER BY file_size DESC")
    suspend fun getByScanTime(scanTime: Long): List<LargeFileEntry>

    @Query("SELECT COUNT(*) FROM large_files")
    suspend fun getCount(): Int

    @Query("SELECT COALESCE(SUM(file_size), 0) FROM large_files")
    suspend fun getTotalSize(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: LargeFileEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<LargeFileEntry>)

    @Query("DELETE FROM large_files")
    suspend fun deleteAll()

    @Query("DELETE FROM large_files WHERE scan_timestamp != :currentScan")
    suspend fun deleteOldScans(currentScan: Long)

    @Query("DELETE FROM large_files WHERE id = :id")
    suspend fun deleteById(id: Long)
}
