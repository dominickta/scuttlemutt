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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import backend.scuttlemutt.Scuttlemutt
import com.scuttlemutt.app.R
import com.scuttlemutt.app.SingletonScuttlemutt
import com.scuttlemutt.app.theme.JetchatTheme

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var mutt: Scuttlemutt

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mutt = SingletonScuttlemutt.getInstance()
        viewModel = ViewModelProvider(requireActivity(), ProfileViewModelFactory(mutt)).get(ProfileViewModel::class.java)
        // Consider using safe args plugin
        val userId = arguments?.getString("name")
        viewModel.setUser(userId!!)
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.fragment_profile, container, false)

        rootView.findViewById<ComposeView>(R.id.profile_compose_view).apply {
            setContent {
                val userData by viewModel.userData.observeAsState()
                val nestedScrollInteropConnection = rememberNestedScrollInteropConnection()

                JetchatTheme {
                    if (userData == null) {
                        ProfileError()
                    } else {
                        ProfileScreen(
                            userData = userData!!,
                            nestedScrollInteropConnection = nestedScrollInteropConnection
                        )
                    }
                }
            }
        }
        return rootView
    }
}
