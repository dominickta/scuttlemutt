package com.scuttlemutt.app.backendimplementations.storagemanager.conversation;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * DAO for ConversationEntry objects.
 */
@Dao
public interface ConversationDao {
    @Query("SELECT * FROM conversationentry WHERE userIdJson LIKE :userIdString LIMIT 1")
    ConversationEntry findByUuid(String userIdString);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertConversationEntry(ConversationEntry conversationEntry);

    @Delete
    int deleteConversationEntry(ConversationEntry conversationEntry);

    @Query("SELECT * FROM conversationentry")
    List<ConversationEntry> listAllConversations();
}
