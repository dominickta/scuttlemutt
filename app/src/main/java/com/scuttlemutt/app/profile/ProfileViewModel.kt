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

package com.scuttlemutt.app.profile

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.*
import backend.scuttlemutt.Scuttlemutt
import com.scuttlemutt.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import types.DawgIdentifier
import java.lang.Math.abs

class ProfileViewModelFactory (private val mutt: Scuttlemutt) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(mutt) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModel(private val mutt: Scuttlemutt) : ViewModel() {

    private val imgs: IntArray = intArrayOf(R.drawable.dog1, R.drawable.dog2, R.drawable.dog3)
    private var username: String = ""

    fun setUser(name: String) {
        if (name != username) {
            username = name
        } else {
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                var dawgIdentifier: DawgIdentifier? = null
                for (id in mutt.allContacts) {
                    if (id.username == username) {
                        dawgIdentifier = id
                    }
                }
                _userData.postValue(ProfileScreenState(
                    name = username,
                    uuid = dawgIdentifier!!.uuid.toString(),
                    photo = imgs[abs(username.hashCode()) % imgs.size],
                ))
            }
        }
    }

    private val _userData = MutableLiveData<ProfileScreenState>()
    val userData: LiveData<ProfileScreenState> = _userData
}

@Immutable
data class ProfileScreenState(
    val name: String,
    val uuid: String,
    @DrawableRes val photo: Int?,
)