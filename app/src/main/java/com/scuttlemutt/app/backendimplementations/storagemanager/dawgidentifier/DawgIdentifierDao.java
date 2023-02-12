package com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.UUID;

/**
 * DAO for DawgIdentifierEntry objects.
 */
@Dao
public interface DawgIdentifierDao {
    @Query("SELECT * FROM dawgidentifierentry WHERE uuid LIKE :dawgIdUuid LIMIT 1")
    DawgIdentifierEntry findByUuid(UUID dawgIdUuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertDawgIdentifierEntry(DawgIdentifierEntry dawgIdentifierEntry);

    @Delete
    int deleteDawgIdentifierEntry(DawgIdentifierEntry dawgIdentifierEntry);
}
