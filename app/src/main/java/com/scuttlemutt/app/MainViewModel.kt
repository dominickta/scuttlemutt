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

import androidx.lifecycle.*
import backend.scuttlemutt.Scuttlemutt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Used to communicate between screens.
 */

class MainViewModelFactory (private val mutt: Scuttlemutt) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(mutt) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainViewModel(private val mutt: Scuttlemutt) : ViewModel() {
    private val TAG = "MainViewModel"

    private val _allContactNames : MutableLiveData<List<String>> = MutableLiveData(listOf())
    val allContactNames: LiveData<List<String>> = _allContactNames

    private val _activeContact = MutableLiveData<String>("me")
    val activeContact: LiveData<String> = _activeContact

    private val _drawerShouldBeOpened = MutableStateFlow(false)
    val drawerShouldBeOpened = _drawerShouldBeOpened.asStateFlow()

    private var contactListUpdater: Job

    init {
        contactListUpdater = viewModelScope.launch(Dispatchers.Default){
            while (coroutineContext.isActive) {
                val contactNames: MutableList<String> = mutableListOf()
                for (id in mutt.allContacts) {
                    contactNames.add(id.userContact)
                }
                _allContactNames.postValue(contactNames)
            }
        }
    }

    fun setChannel(newChan: String) {
        _activeContact.value = newChan
    }

    fun openDrawer() {
        _drawerShouldBeOpened.value = true
    }

    fun resetOpenDrawerAction() {
        _drawerShouldBeOpened.value = false
    }
}
