package com.scuttlemutt.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object that uses SQL to interact with the Contacts table
 *
 * Notes:
 * - suspend means the query will run in a background thread
 * - Returning a Flow enables us to only run a query once and have it run in the background,
 *   and also automatically update when the data changes.
 * - These are abstract methods because Room will automatically generate their implementations for us
 */


@Dao
interface BarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // TODO: pick conflict strategy
    suspend fun insert(bark: Bark)

    @Update
    suspend fun update(bark: Bark)

    @Delete
    suspend fun delete(bark: Bark)

    @Query("SELECT * FROM barks WHERE dstPublicKey = :dstPublicKey AND srcPublicKey = :srcPublicKey ORDER BY seqNum DESC")
    fun getBarks(srcPublicKey: String, dstPublicKey: String): Flow<List<Bark>>

    @Query("SELECT seqNum FROM barks WHERE dstPublicKey = :dstPublicKey AND srcPublicKey = :srcPublicKey ORDER BY seqNum DESC LIMIT 1")
    suspend fun getLastSeqNum(srcPublicKey: String, dstPublicKey: String): Int?
}