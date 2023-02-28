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

package com.scuttlemutt.app

import android.util.Log
import androidx.lifecycle.*
import backend.scuttlemutt.Scuttlemutt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import types.Conversation
import types.DawgIdentifier

/**
 * Used to communicate between screens.
 */

class MainViewModelFactory (private val mutt: Scuttlemutt, private val navActivity: NavActivity) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(mutt, navActivity) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainViewModel(private val mutt: Scuttlemutt, private val navActivity: NavActivity) : ViewModel() {
    private val TAG = "MainViewModel"

    // Map of (username, total # of chat messages in convo when last clicked)
    private val convSizes : MutableMap<String, Int> = mutableMapOf()
    // Map of (username, # of new chat messages since last clicked)
    private val _allContactNames : MutableLiveData<MutableMap<String, Int>> = MutableLiveData(mutableMapOf())
    val allContactNames: LiveData<MutableMap<String, Int>> = _allContactNames

    private val _activeContact = MutableLiveData<String>(mutt.dawgIdentifier.username)
    val activeContact: LiveData<String> = _activeContact

    private val _drawerShouldBeOpened = MutableStateFlow(false)
    val drawerShouldBeOpened = _drawerShouldBeOpened.asStateFlow()

    private var contactListUpdater: Job

    init {
        contactListUpdater = viewModelScope.launch(Dispatchers.Default){
            while (coroutineContext.isActive) {
                // This must be cloned to trigger recomposition for the observable live data
                val contactNames: MutableMap<String, Int> = _allContactNames.value!!.toMutableMap()
                val allContacts: List<DawgIdentifier> = mutt.allContacts
                if (allContacts.isEmpty()) {
                    continue
                }
                val allConvs: List<Conversation> = mutt.listAllConversations()
                for (id in allContacts) {
                    if (id == null) {
                        continue
                    }
                    // can't get this to work, always throws NPE even if {mutt, id, id.uuid} is not null
                    // (the stack trace stops at mutt.getConversation(id), doesn't even go inside)
                    // var conv : Conversation = mutt.getConversation(id)
                    // Therefore, looping through to manually find the conversation:
                    var conv: Conversation? = null
                    for (c in allConvs) {
                        if (c.otherPerson == id) {
                            conv = c
                        }
                    }
                    val currNumMessagesInConv = if (conv == null) 0 else mutt.getMessagesForConversation(conv).size
                    if (contactNames.containsKey(id.username) && _activeContact.value != id.username) {
                        contactNames.put(id.username, currNumMessagesInConv - convSizes.getValue(id.username))
                    } else {
                        convSizes.put(id.username, currNumMessagesInConv)
                        contactNames.put(id.username, 0)
                    }
                }
                _allContactNames.postValue(contactNames)
            }
        }
    }

    val mainLiveData = navActivity.testLiveData
    fun testCallNavActivity() {
        navActivity.testPrint()
    }

    fun updateLiveString(input: String) {
        navActivity.testChangeString(input)
    }

    fun setChannel(newChan: String) {
        _activeContact.value = newChan
        // this didn't work because of NPE stuff: var conv : Conversation = mutt.getConversation(id)
        // therefore, I'll loop through and manually find it...
        val allConvs: List<Conversation> = mutt.listAllConversations()
        for (conv in allConvs) {
            if (conv.otherPerson.username == newChan) {
                convSizes.put(newChan, mutt.getMessagesForConversation(conv).size)
                break;
            }
        }
        _allContactNames.value!!.put(newChan, 0)
    }

    fun openDrawer() {
        _drawerShouldBeOpened.value = true
    }

    fun resetOpenDrawerAction() {
        _drawerShouldBeOpened.value = false
    }
}
