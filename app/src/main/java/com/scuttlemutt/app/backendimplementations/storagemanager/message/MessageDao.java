package com.scuttlemutt.app.backendimplementations.storagemanager.message;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

/**
 * DAO for MessageEntry objects.
 */
@Dao
public interface MessageDao {
    @Query("SELECT * FROM MessageEntry WHERE uuid LIKE :messageUuid LIMIT 1")
    MessageEntry findByUuid(String messageUuid);

    @Insert
    long insertMessageEntry(MessageEntry messageEntry);

    @Delete
    int deleteMessageEntry(MessageEntry messageEntry);
}
