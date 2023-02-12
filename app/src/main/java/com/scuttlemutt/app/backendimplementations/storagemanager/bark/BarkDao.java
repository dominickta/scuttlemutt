package com.scuttlemutt.app.backendimplementations.storagemanager.bark;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.UUID;

/**
 * DAO for BarkEntry objects.
 */
@Dao
public interface BarkDao {
    @Query("SELECT * FROM barkentry WHERE uuid LIKE :barkUuid LIMIT 1")
    BarkEntry findByUuid(UUID barkUuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertBarkEntry(BarkEntry barkEntry);

    @Delete
    int deleteBarkEntry(BarkEntry barkEntry);
}
