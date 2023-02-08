/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.compose.jetchat

import androidx.lifecycle.*
import com.example.compose.jetchat.conversation.ConversationUiState
import com.example.compose.jetchat.conversation.ConversationViewModel
import com.example.compose.jetchat.data.Contact
import com.example.compose.jetchat.data.ScuttlemuttDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Used to communicate between screens.
 */

class MainViewModelFactory (private val database: ScuttlemuttDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainViewModel(private val database: ScuttlemuttDatabase) : ViewModel() {

    private val _allContactNames : MutableLiveData<List<String>> = MutableLiveData(listOf())
    val allContactNames: LiveData<List<String>> = _allContactNames

    private val _activeChannel = MutableLiveData<String>("FriendA") // TODO: change to contact
    val activeChannel: LiveData<String> = _activeChannel

    private val _drawerShouldBeOpened = MutableStateFlow(false)
    val drawerShouldBeOpened = _drawerShouldBeOpened.asStateFlow()

    init {
        viewModelScope.launch {
            database.contactDao().insert(Contact(publicKey = "myPublicKey", nickname = "me"))
            database.contactDao().insert(Contact(publicKey = "FriendAPublicKey", nickname = "FriendA"))
            database.contactDao().insert(Contact(publicKey = "FriendBPublicKey", nickname = "FriendB"))
            database.contactDao().getAllContacts().collect {
                val contactNames : MutableList<String> = mutableListOf()
                for (contact in it) {
                    contactNames.add(contact.nickname)
                }
                _allContactNames.value = contactNames
                _activeChannel.value = it[0].nickname
            }
        }
    }

    fun setChannel(newChan: String) {
        _activeChannel.value = newChan
    }

    fun openDrawer() {
        _drawerShouldBeOpened.value = true
    }

    fun resetOpenDrawerAction() {
        _drawerShouldBeOpened.value = false
    }
}
