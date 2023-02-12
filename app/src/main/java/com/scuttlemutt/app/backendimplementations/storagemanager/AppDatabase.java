package com.scuttlemutt.app.backendimplementations.storagemanager;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.scuttlemutt.app.backendimplementations.storagemanager.bark.BarkEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier.DawgIdentifierEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.bark.BarkDao;
import com.scuttlemutt.app.backendimplementations.storagemanager.conversation.ConversationDao;
import com.scuttlemutt.app.backendimplementations.storagemanager.conversation.ConversationEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier.DawgIdentifierDao;

/**
 * This class represents the Room DB object used to interact with the various data types we store in
 * our DB.
 */
@Database(entities = {BarkEntry.class, ConversationEntry.class, DawgIdentifierEntry.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BarkDao barkDao();
    public abstract ConversationDao conversationDao();
    public abstract DawgIdentifierDao dawgIdentifierDao();
}
