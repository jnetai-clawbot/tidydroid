package com.jnetaol.tidydroid.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jnetaol.tidydroid.data.model.*
import com.jnetaol.tidydroid.logger.DebugLogger

@Database(entities = [SortRule::class, ScanResult::class, FileDuplicate::class, LargeFileEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rulesDao(): RulesDao
    abstract fun scanResultsDao(): ScanResultsDao
    abstract fun fileDuplicatesDao(): FileDuplicatesDao
    abstract fun largeFilesDao(): LargeFilesDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context): AppDatabase = try {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "tidydroid.db")
                .fallbackToDestructiveMigration().build()
        } catch (e: Exception) {
            DebugLogger.e("AppDatabase", "DB creation failed", "TD-DB-001", e)
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "tidydroid_fallback.db")
                .fallbackToDestructiveMigration().build()
        }
    }
}
