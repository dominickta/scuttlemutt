package com.example.compose.jetchat.data

import kotlinx.coroutines.flow.Flow
import java.security.PublicKey

///**
// * Implementation of the ScuttlemuttRepository interface that uses a Room database
// */
//class RoomScuttlemuttRepository(private val contactDao: ContactDao) : ScuttlemuttRepository {
//
//    override fun getAllContactsStream(): Flow<List<Contact>> = contactDao.getAllContacts()
//
//    override fun getContactStream(publicKey: PublicKey): Flow<Contact?> = contactDao.getContact(publicKey)
//
//    override suspend fun insertContact(contact: Contact) = contactDao.insert(contact)
//
//    override suspend fun deleteContact(contact: Contact) = contactDao.delete(contact)
//
//    override suspend fun updateContact(contact: Contact) = contactDao.update(contact)
//}