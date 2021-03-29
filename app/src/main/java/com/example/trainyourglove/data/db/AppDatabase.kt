package com.example.trainyourglove.data.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.trainyourglove.data.db.daos.GesturesDao
import com.example.trainyourglove.data.db.entities.Gesture

@Database(entities = [Gesture::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gesturesDao(): GesturesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(application: Application): AppDatabase {
            var localRef = INSTANCE

            return localRef ?: synchronized(AppDatabase::class.java) {
                localRef = INSTANCE

                localRef ?: Room.databaseBuilder(
                    application,
                    AppDatabase::class.java, "com.example.gg_trainer_db",
                ).fallbackToDestructiveMigration()
                    .build().apply {
                        INSTANCE = this
                    }
            }
        }
    }
}