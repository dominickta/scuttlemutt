package com.example.compose.jetchat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The singleton database instance that holds all contact & conversation info we care about.
 *
 * It is an abstract class because Room will automatically generate its implementation
 */

@Database(entities = [Contact::class, Bark::class], version = 2, exportSchema = false)
abstract class ScuttlemuttDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun barkDao(): BarkDao

    companion object {
        @Volatile // The value of a volatile variable is never cached, and all reads and writes are to and from the main memory. These features help ensure the value of Instance is always up to date and is the same for all execution threads. It means that changes made by one thread to Instance are immediately visible to all other threads.
        private var Instance: ScuttlemuttDatabase? = null

        fun getDatabase(context: Context): ScuttlemuttDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ScuttlemuttDatabase::class.java, "scuttlemutt_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}