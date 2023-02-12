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

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import backend.scuttlemutt.Scuttlemutt
import com.scuttlemutt.app.components.JetchatDrawer
import com.scuttlemutt.app.conversation.BackPressHandler
import com.scuttlemutt.app.conversation.LocalBackPressedDispatcher
import com.scuttlemutt.app.databinding.ContentMainBinding
import kotlinx.coroutines.launch
import java.util.*


/**
 * Main activity for the app.
 */
class NavActivity : AppCompatActivity() {

    private val TAG = "NavActivity"
    private lateinit var viewModel: MainViewModel
    private lateinit var mutt: Scuttlemutt

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "GETTING SCUTTLEMUTT INSTANCE")
        mutt = SingletonScuttlemutt.getInstance(this)
        viewModel = ViewModelProvider(this, MainViewModelFactory(mutt)).get(MainViewModel::class.java)
        Log.d(TAG, "My name is ${mutt.dawgIdentifier}")

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(
            ComposeView(this).apply {
                consumeWindowInsets = false
                setContent {
                    CompositionLocalProvider(
                        LocalBackPressedDispatcher provides this@NavActivity.onBackPressedDispatcher
                    ) {
                        val drawerState = rememberDrawerState(initialValue = Closed)
                        val drawerOpen by viewModel.drawerShouldBeOpened
                            .collectAsStateWithLifecycle()

                        val activeChannel by viewModel.activeContact.observeAsState()

                        if (drawerOpen) {
                            // Open drawer and reset state in VM.
                            LaunchedEffect(Unit) {
                                // wrap in try-finally to handle interruption whiles opening drawer
                                try {
                                    drawerState.open()
                                } finally {
                                    viewModel.resetOpenDrawerAction()
                                }
                            }
                        }

                        // Intercepts back navigation when the drawer is open
                        val scope = rememberCoroutineScope()
                        if (drawerState.isOpen) {
                            BackPressHandler {
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        }

                        JetchatDrawer(
                            viewModel = viewModel,
                            activeChannel = activeChannel!!,
                            drawerState = drawerState,
                            onChatClicked = fun(channel: String){
                                viewModel.setChannel(channel)
                                Log.d("NavActivity", "Navigating to nav_home")
                                findNavController().navigate(R.id.nav_home)
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            onProfileClicked = {
                                val bundle = bundleOf("userId" to it)
                                Log.d("NavActivity", "Navigating to nav_profile")
                                findNavController().navigate(R.id.nav_profile, bundle)
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        ) {
                            AndroidViewBinding(ContentMainBinding::inflate)
                        }
                    }
                }
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController().navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * See https://issuetracker.google.com/142847973
     */
    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }
}
