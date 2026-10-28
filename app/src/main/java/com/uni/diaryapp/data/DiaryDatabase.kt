package com.uni.diaryapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uni.diaryapp.data.Dao.DiaryDao
import com.uni.diaryapp.data.Dao.TodoDao
import com.uni.diaryapp.data.model.DiaryEntry
import com.uni.diaryapp.data.model.TodoItem

@Database(entities = [DiaryEntry::class, TodoItem::class], version = 16, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DiaryDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao
    abstract fun todoDao(): TodoDao

    companion object {
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE diary_entries ADD COLUMN ownerUid TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE todos ADD COLUMN ownerUid TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        @Volatile
        private var INSTANCE: DiaryDatabase? = null


        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_db"
                )
                    .addMigrations(MIGRATION_15_16)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
