package com.uni.diaryapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uni.diaryapp.data.Dao.DiaryDao
import com.uni.diaryapp.data.Dao.TodoDao
import com.uni.diaryapp.data.model.DiaryEntry
import com.uni.diaryapp.data.model.TodoItem

@Database(entities = [DiaryEntry::class, TodoItem::class], version = 14, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DiaryDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
