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

package com.example.compose.jetchat.conversation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.toMutableStateList
import com.example.compose.jetchat.R
import com.example.compose.jetchat.data.Bark

class ConversationUiState(
//    val channelName: String,
//    val channelMembers: Int,
    var contactName: String,
    var messages: List<FrontEndMessage>
) {
//    private val _messages: MutableList<Message> = initialMessages.toMutableStateList()
}

@Immutable
data class FrontEndMessage(
    val author: String,
    val content: String,
    val timestamp: String,
//    val image: Int? = null, // for messages that have an img attachment
    val authorImage: Int = if (author == "me") R.drawable.ali else R.drawable.someone_else
)
