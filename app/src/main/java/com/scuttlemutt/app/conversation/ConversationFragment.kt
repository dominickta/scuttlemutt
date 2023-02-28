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

package com.scuttlemutt.app.conversation

import ConversationViewModel
import ConversationViewModelFactory
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import backend.scuttlemutt.Scuttlemutt
import com.scuttlemutt.app.R
import com.scuttlemutt.app.SingletonScuttlemutt
import com.scuttlemutt.app.theme.JetchatTheme

class ConversationFragment : Fragment() {
    private val TAG = "ConversationFragment"
    private val activityViewModel: com.scuttlemutt.app.MainViewModel by activityViewModels()

    private lateinit var conversationViewModel: ConversationViewModel

    private lateinit var mutt: Scuttlemutt

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "GETTING SCUTTLEMUTT INSTANCE")
        mutt = SingletonScuttlemutt.getInstance()
        val activeChat = activityViewModel.activeContact.value!!
        conversationViewModel = ViewModelProvider(requireActivity(), ConversationViewModelFactory(mutt, activeChat)).get(ConversationViewModel::class.java)
        conversationViewModel.setChat(activeChat)
        Log.d(TAG, "My name is ${mutt.dawgIdentifier}")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(inflater.context).apply {
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        Log.d(TAG, "ONCREATEVIEW")
        setContent {
            val currUiState by conversationViewModel.currUiState.observeAsState()
            Log.d(TAG, "recomposing ${currUiState!!.contactName}")

            CompositionLocalProvider(
                LocalBackPressedDispatcher provides requireActivity().onBackPressedDispatcher
            ) {
                JetchatTheme {
                    ConversationContent(
                        conversationViewModel = conversationViewModel,
                        uiState = currUiState!!,
                        navigateToProfile = { user ->
                            // Click callback
                            val bundle = bundleOf("name" to user)
                            findNavController().navigate(
                                R.id.nav_profile,
                                bundle
                            )
                        },
                        onNavIconPressed = {
                            activityViewModel.openDrawer()
                        },
                        // Add padding so that we are inset from any navigation bars
                        modifier = Modifier.windowInsetsPadding(
                            WindowInsets
                                .navigationBars
                                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        )
                    )
                }
            }
        }
    }
}
