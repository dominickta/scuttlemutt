package com.scuttlemutt.app

import android.content.Context
import android.util.Log
import androidx.room.Room
import backend.iomanager.IOManager
import backend.iomanager.QueueIOManager
import backend.scuttlemutt.Scuttlemutt
import com.scuttlemutt.app.backendimplementations.storagemanager.AppDatabase
import com.scuttlemutt.app.backendimplementations.storagemanager.RoomStorageManager
import crypto.Crypto
import storagemanager.StorageManager
import types.DawgIdentifier
import java.security.KeyPair
import java.util.*

/*
The NavActivity (the entry point for our app) should/does instantiate the Scuttlemutt with its
 context before any thing else retrieves an instance. Therefore, only NavActivity should call
 getInstance(context), and any other Android thing who needs an instance should call getInstance().

 This is because after NavActivity calls getInstance(context), any future calls with getInstance(context)
 would not have that context used because an instance already exists. Therefore for clarity, only
 getInstance() should be used.

 NOTE: No idea what the context is used for/what it affects, but good to keep it clear.
 */
class SingletonScuttlemutt {
    companion object {
        /*
        The value of a volatile variable is never cached, and all reads and writes are to and
        from the main memory. These features help ensure the value of Instance is always up to date
        and is the same for all execution threads. It means that changes made by one thread to
        Instance are immediately visible to all other threads.
         */
        @Volatile
        private var INSTANCE: Scuttlemutt? = null

        // Should only be called by NavActivity
        fun getInstance(context: Context): Scuttlemutt {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        val mykeys: KeyPair = Crypto.generateKeyPair()
                        val iom: IOManager = QueueIOManager()
                        val dawgid: DawgIdentifier = DawgIdentifier("me", UUID.fromString("22df6593-676e-4c8c-a9d9-48d43c03cc8e"), mykeys.public)

                        val appDb : AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "scuttlemutt-app-database")
                            .fallbackToDestructiveMigration()
                            .build()
                        val storagem: StorageManager = RoomStorageManager(appDb)
                        val mutt: Scuttlemutt = Scuttlemutt(iom, dawgid, storagem)
                        Log.d("SingletonScuttlemutt", "instantiating instance..: ${mutt.dawgIdentifier}")
                        INSTANCE = mutt
                    }
                }
            }
            return INSTANCE!!
        }

        // Anything else can call this
        fun getInstance(): Scuttlemutt {
            // If this assertion fails, some other thing getInstance() before NavActivity
            assert(INSTANCE != null)
            return INSTANCE!!
        }
    }
}