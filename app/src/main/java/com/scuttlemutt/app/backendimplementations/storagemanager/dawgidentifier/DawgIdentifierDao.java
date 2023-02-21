package com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * DAO for DawgIdentifierEntry objects.
 */
@Dao
public interface DawgIdentifierDao {
    @Query("SELECT * FROM dawgidentifierentry WHERE uuid LIKE :dawgIdUuid LIMIT 1")
    DawgIdentifierEntry findByUuid(String dawgIdUuid);

    @Query("SELECT * FROM dawgidentifierentry WHERE userContact LIKE :userContact LIMIT 1")
    DawgIdentifierEntry findByUserContact(String userContact);

    @Query("SELECT * FROM dawgidentifierentry")
    List<DawgIdentifierEntry> getAllDawgIdentifiers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertDawgIdentifierEntry(DawgIdentifierEntry dawgIdentifierEntry);

    @Delete
    int deleteDawgIdentifierEntry(DawgIdentifierEntry dawgIdentifierEntry);
}
