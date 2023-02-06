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

package com.example.compose.jetchat.data

import com.example.compose.jetchat.R
import com.example.compose.jetchat.conversation.ConversationUiState
import com.example.compose.jetchat.conversation.FrontEndMessage
import com.example.compose.jetchat.conversation.Message
import com.example.compose.jetchat.profile.ProfileScreenState

//val composersMessages = listOf(
//    FrontEndMessage(
//        "me",
//        "Check it out!",
//        "8:07 PM"
//    ),
//    FrontEndMessage(
//        "me",
//        "Thank you!",
//        "8:06 PM",
////        R.drawable.sticker
//    ),
//    FrontEndMessage(
//        "Taylor Brooks",
//        "You can use all the same stuff",
//        "8:05 PM"
//    ),
//    FrontEndMessage(
//        "Taylor Brooks",
//        "@aliconors Take a look at the `Flow.collectAsStateWithLifecycle()` APIs",
//        "8:05 PM"
//    ),
//    FrontEndMessage(
//        "John Glenn",
//        "Compose newbie as well, have you looked at the JetNews sample? Most blog posts end up " +
//            "out of date pretty fast but this sample is always up to date and deals with async " +
//            "data loading (it's faked but the same idea applies) \uD83D\uDC49" +
//            "https://github.com/android/compose-samples/tree/master/JetNews",
//        "8:04 PM"
//    ),
//    FrontEndMessage(
//        "me",
//        "Compose newbie: I’ve scourged the internet for tutorials about async data loading " +
//            "but haven’t found any good ones. What’s the recommended way to load async " +
//            "data and emit composable widgets?",
//        "8:03 PM"
//    )
//)
//
//val droidconMessages = listOf(
//    FrontEndMessage(
//        "me",
//        "Let's check out droid con!!!!",
//        "8:07 PM"
//    ),
//    FrontEndMessage(
//        "me",
//        "Thank you!",
//        "8:06 PM",
//    ),
//    FrontEndMessage(
//        "Taylor Brooks",
//        "Have you heard of it?",
//        "8:05 PM"
//    ),
//    FrontEndMessage(
//        "Taylor Brooks",
//        "@aliconors Take a look at DroidCon NYC!!!",
//        "8:05 PM"
//    ),
//    FrontEndMessage(
//        "John Glenn",
//        "Compose newbie as well, have you looked at the JetNews sample? Most blog posts end up " +
//                "out of date pretty fast but this sample is always up to date and deals with async " +
//                "data loading (it's faked but the same idea applies) \uD83D\uDC49" +
//                "https://github.com/android/compose-samples/tree/master/JetNews",
//        "8:04 PM"
//    ),
//    FrontEndMessage(
//        "me",
//        "Compose newbie: I’ve scourged the internet for tutorials about async data loading " +
//                "but haven’t found any good ones. What’s the recommended way to load async " +
//                "data and emit composable widgets?",
//        "8:03 PM"
//    )
//)
//
//val exampleUiStateComposers = ConversationUiState(
//    messages = composersMessages,
//    contactName = "#composers",
//)
//
//val exampleUiStateDroidConNYC = ConversationUiState(
//    messages = droidconMessages, // would load this in from database
//    contactName = "#droidcon-nyc",
//)

/**
 * Example colleague profile
 */
val colleagueProfile = ProfileScreenState(
    userId = "12345",
    photo = R.drawable.someone_else,
    name = "Taylor Brooks",
    status = "Away",
    displayName = "taylor",
    position = "Senior Android Dev at Openlane",
    twitter = "twitter.com/taylorbrookscodes",
    timeZone = "12:25 AM local time (Eastern Daylight Time)",
    commonChannels = "2"
)

/**
 * Example "me" profile.
 */
val meProfile = ProfileScreenState(
    userId = "me",
    photo = R.drawable.ali,
    name = "Ali Conors",
    status = "Online",
    displayName = "aliconors",
    position = "Senior Android Dev at Yearin\nGoogle Developer Expert",
    twitter = "twitter.com/aliconors",
    timeZone = "In your timezone",
    commonChannels = null
)
