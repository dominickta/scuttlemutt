package com.scuttlemutt.app.data
//
//import kotlinx.coroutines.flow.Flow
//import java.security.PublicKey
//
///**
// * Repository to be used by frontend ViewModel and backend Scuttlemutt I/O to interact with
// * the underlying data
// *
// * Note: Flows automatically update (hence the stream)
// *
// * Tutorial: https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room#7
// */
//interface ScuttlemuttRepository {
//
//    /**
//     * Retrieves all known contacts
//     */
//    fun getAllContactsStream(): Flow<List<Contact>>
//
//    /**
//     * Retrieve information about a known contact given their public key
//     *
//     * Note that the return type is nullable.
//     */
//    fun getContactStream(publicKey: PublicKey): Flow<Contact?>
//
//    /**
//     * Insert a new contact
//     */
//    suspend fun insertContact(contact: Contact)
//
//    /**
//     * Delete a contact
//     */
//    suspend fun deleteContact(contact: Contact)
//
//    /**
//     * Update a contact
//     */
//    suspend fun updateContact(contact: Contact)
//}