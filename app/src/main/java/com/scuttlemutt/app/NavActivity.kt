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

//import com.example.compose.jetchat.databinding.ContentMainBinding
import android.os.Bundle
import android.util.Log
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
import backend.iomanager.IOManager
import backend.iomanager.QueueIOManager
import backend.scuttlemutt.Scuttlemutt
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import com.scuttlemutt.app.components.JetchatDrawer
import com.scuttlemutt.app.connection.ConnectionsActivity
import com.scuttlemutt.app.conversation.BackPressHandler
import com.scuttlemutt.app.conversation.LocalBackPressedDispatcher
import com.scuttlemutt.app.data.ScuttlemuttDatabase
import com.scuttlemutt.app.databinding.ContentMainBinding
import crypto.Crypto
import kotlinx.coroutines.launch
import storagemanager.MapStorageManager
import storagemanager.StorageManager
import types.DawgIdentifier
import types.packet.BarkPacket
import java.security.KeyPair
import java.util.*


/**
 * Main activity for the app.
 */
class NavActivity() : ConnectionsActivity() {

    private lateinit var viewModel: MainViewModel


    private val SERVICE_ID = "com.google.location.nearby.apps.walkietalkie.automatic.SERVICE_ID"

    /**
     * The state of the app. As the app changes states, the UI will update and advertising/discovery
     * will start/stop.
     */
    private var mState: com.scuttlemutt.app.NavActivity.State =
        com.scuttlemutt.app.NavActivity.State.UNKNOWN

    /**
     * Name of the user - ScuttleMutt.DawgIdentifier.userContact
     */
    override var name = "Placeholder"

    /**
     * This service id lets us find other nearby devices that are interested in the same thing. Our
     * sample does exactly one thing, so we hardcode the ID.
     */
    override val serviceId = "com.google.location.nearby.apps.walkietalkie.automatic.SERVICE_ID"

    override val strategy = Strategy.P2P_STAR

    override val TAG = "NavActivity"
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = ScuttlemuttDatabase.getDatabase(this)
        viewModel = ViewModelProvider(this, MainViewModelFactory(database)).get(MainViewModel::class.java)

        val mykeys: KeyPair = Crypto.generateKeyPair()
        val iom: IOManager = QueueIOManager()
        val dawgid: DawgIdentifier = DawgIdentifier("blah", UUID.randomUUID(), mykeys.public)
        val storagem: StorageManager = MapStorageManager()

        val mutt: Scuttlemutt = Scuttlemutt(iom, dawgid, storagem)
        Log.d(TAG, "Testing Scuttlemutt dawgIdentifier: ${mutt.dawgIdentifier}")

        // Set user name
        name = mutt.dawgIdentifier.userContact
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

                        val activeChannel by viewModel.activeChannel.observeAsState()

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
//                                convViewModel.setChat(channel)
                                viewModel.setChannel(channel)
//                                val bundle = bundleOf("channel" to it)
//                                Log.d("NavActivity", "Navigating to nav_home")
//                                findNavController().navigate(R.id.nav_home, bundle)
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

    override fun onStart() {
        super<ConnectionsActivity>.onStart()
        setState(com.scuttlemutt.app.NavActivity.State.SEARCHING)
    }

    override fun onStop(){

        // After our Activity stops, we disconnect from Nearby Connections.
        setState(com.scuttlemutt.app.NavActivity.State.SEARCHING)
        super.onStop()
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

    /**
     * Send provided string.
     */
    private fun onSend(message: BarkPacket) {
        logV("startSend()")

        val byteMessage: ByteArray = message.toNetworkBytes()
        send(Payload.fromBytes(byteMessage))
    }
    override fun onReceive(endpoint: Endpoint?, payload: Payload?) {
        logD("" + payload?.type)
        if (payload?.type == Payload.Type.BYTES) {
                val buffer = payload?.asBytes()
                var messageString = String(buffer!!)
            if (endpoint != null) {
                messageString = "From " + endpoint.name + ": " + messageString
            }
                logI(messageString, false)
                logD("RECIEVED MESSAGE")
            }
        }

    override fun onEndpointDiscovered(endpoint: Endpoint) {
        // We found an advertiser!
        stopDiscovering()
        connectToEndpoint(endpoint!!)
    }
    protected fun verifyConnection(): Boolean {
        return true
    }
    override fun onConnectionInitiated(endpoint: Endpoint, connectionInfo: ConnectionInfo) {
        // We accept the connection immediately.
        // TODO: Put some verification with IOManager here.
        // For now, dummy method that just returns true
        if(verifyConnection()){
            acceptConnection(endpoint)
        }else{
            rejectConnection(endpoint)
        }

    }

    override fun onEndpointConnected(endpoint: Endpoint?) {
        // Maybe a message to say we've been connected?
        logD("CONNECTED")
        setState(com.scuttlemutt.app.NavActivity.State.CONNECTED)
    }

    override fun onEndpointDisconnected(endpoint: Endpoint?) {
        // Maybe a message to say we've been disconnected?
        setState(com.scuttlemutt.app.NavActivity.State.SEARCHING)
    }

    override fun onConnectionFailed(endpoint: Endpoint?) {
        // Let's try someone else.
        if (getState() == com.scuttlemutt.app.NavActivity.State.SEARCHING) {
            startDiscovering()
        }
    }
    /**
     * The state has changed. Switch it.
     *
     * @param state The new state.
     */
    private fun setState(state: com.scuttlemutt.app.NavActivity.State) {
        if (mState == state) {
            logW("State set to $state but already in that state")
            return
        }
        logD("State set to $state")
        val oldState: com.scuttlemutt.app.NavActivity.State = mState
        mState = state
        onStateChanged(oldState, state)
    }

    /** @return The current state.
     */
    private fun getState(): com.scuttlemutt.app.NavActivity.State? {
        return mState
    }

    /**
     * State has changed.
     *
     * @param oldState The previous state we were in. Clean up anything related to this state.
     * @param newState The new state we're now in. Prepare the UI for this state.
     */
    private fun onStateChanged(
        oldState: com.scuttlemutt.app.NavActivity.State,
        newState: com.scuttlemutt.app.NavActivity.State
    ) {
        when (newState) {
            com.scuttlemutt.app.NavActivity.State.SEARCHING -> {
                disconnectFromAllEndpoints()
                startDiscovering()
                startAdvertising()
            }
            com.scuttlemutt.app.NavActivity.State.CONNECTED -> {
                stopDiscovering()
                stopAdvertising()
            }
            com.scuttlemutt.app.NavActivity.State.UNKNOWN -> stopAllEndpoints()
            else -> {}
        }
    }
    /** States that the UI goes through.  */
    enum class State {
        UNKNOWN, SEARCHING, CONNECTED
    }
}
