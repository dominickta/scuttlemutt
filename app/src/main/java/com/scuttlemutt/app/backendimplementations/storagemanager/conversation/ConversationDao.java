package com.scuttlemutt.app.backendimplementations.storagemanager.conversation;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.UUID;

/**
 * DAO for ConversationEntry objects.
 */
@Dao
public interface ConversationDao {
    @Query("SELECT * FROM conversationentry WHERE userUuidListJson LIKE :userUuidListString LIMIT 1")
    ConversationEntry findByUuidList(String userUuidListString);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertConverationEntry(ConversationEntry conversationEntry);

    @Delete
    int deleteConverationEntry(ConversationEntry conversationEntry);

    @Query("SELECT * FROM conversationentry")
    List<ConversationEntry> listAllConversations();
}
