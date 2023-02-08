package com.example.compose.jetchat.data

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
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE) // TODO: pick conflict strategy
    suspend fun insert(contact: Contact)

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("SELECT * from contacts where publicKey = :publicKey")
    suspend fun getContactByKey(publicKey: String): Contact

    @Query("SELECT * from contacts where nickname = :nickname")
    suspend fun getContactByNickname(nickname: String): Contact

    @Query("SELECT * from contacts ORDER BY nickname ASC")
    fun getAllContacts(): Flow<List<Contact>>
}