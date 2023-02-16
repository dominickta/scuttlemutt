package com.scuttlemutt.app.backendimplementations.storagemanager.key;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * DAO for DawgIdentifierEntry objects.
 */
@Dao
public interface KeyDao {
    @Query("SELECT * FROM KeyEntry WHERE uuid LIKE :dawgIdUuid LIMIT 1")
    KeyEntry findByUuid(String dawgIdUuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertKeyEntry(KeyEntry keyEntry);

    @Delete
    int deleteKeyEntry(KeyEntry keyEntry);
}
