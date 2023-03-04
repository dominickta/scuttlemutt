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
import android.provider.Settings
import android.util.Log
import android.widget.Toast
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import backend.initialization.KeyExchanger
import com.scuttlemutt.app.backendimplementations.iomanager.EndpointIOManager
import backend.scuttlemutt.Scuttlemutt
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import com.scuttlemutt.app.components.JetchatDrawer
import com.scuttlemutt.app.connection.ConnectionsActivity
import com.scuttlemutt.app.conversation.BackPressHandler
import com.scuttlemutt.app.conversation.LocalBackPressedDispatcher
import com.scuttlemutt.app.databinding.ContentMainBinding
import crypto.Crypto
import kotlinx.coroutines.launch
import types.packet.Packet
import java.lang.Math.abs
import java.util.*


/**
 * Main activity for the app.
 */
class NavActivity() : ConnectionsActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var mutt: Scuttlemutt



    /**
     * The state of the app. As the app changes states, the UI will update and advertising/discovery
     * will start/stop.
     */
    private var mState: State = State.UNKNOWN

    private lateinit var iom: EndpointIOManager

    private lateinit var keyExchanger: KeyExchanger
    /**
     * Name of the user - ScuttleMutt.DawgIdentifier.username
     */
    override lateinit var name: String

    private lateinit var endpointUUID: String

    /**
     * This service id lets us find other nearby devices that are interested in the same thing. Our
     * sample does exactly one thing, so we hardcode the ID.
     */
    override val serviceId = "com.scuttlemutt.app.service_id.meshing"

    override val strategy = Strategy.P2P_STAR

    override val TAG = "NavActivity"

    // We decided to use random names from a pre-defined list, rather than
    // allow users to pick their own names. This was a simple solution given the time we had.
    private val dawg_names = arrayOf("max", "luna", "milo", "bella", "cooper", "buddy", "lola", "leo")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "GETTING SCUTTLEMUTT INSTANCE")
        // Set user name based off device ID and a human-readable dog name
        val deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)
        name = dawg_names[abs(deviceId.hashCode()) % dawg_names.size] + "#" + deviceId
        mutt = SingletonScuttlemutt.getInstance(this, this.mConnectionsClient!!, name)
        if (!mutt.haveContact(mutt.dawgIdentifier.uuid)) {
            Log.d(TAG, "Adding myself because I'm not in the database yet")
            val myKeyPair = Crypto.generateKeyPair()
            val mySecretKey = Crypto.generateSecretKey()
            mutt.addContact(mutt.dawgIdentifier, myKeyPair.public, mySecretKey)
            mutt.sendMessage("This is a conversation with yourself! Feel free to add notes here or whatever else.", mutt.dawgIdentifier)
        }
        iom = SingletonScuttlemutt.getIOManager()
        keyExchanger = SingletonScuttlemutt.getKeyExchanger()
        viewModel = ViewModelProvider(this, MainViewModelFactory(mutt, this)).get(MainViewModel::class.java)
        Log.d(TAG, "My name is ${mutt.dawgIdentifier}")
        endpointUUID = mutt.dawgIdentifier.uuid.toString()
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
                            onConnectionsClicked = {
                                Log.d("NavActivity", "Navigating to nav_connections")
                                findNavController().navigate(R.id.nav_connections)
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            onChatClicked = fun(channel: String) {
//                                convViewModel.setChat(channel)
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


    private val _testLiveData = MutableLiveData<String>("Alice")
    val testLiveData: LiveData<String> = _testLiveData
    fun testPrint() {
        Log.d(TAG, "navactivity printing!")
    }
    fun testChangeString(input: String) {
        _testLiveData.value = input
    }

    override fun onStart() {
        super<ConnectionsActivity>.onStart()
        logD("STARTING")
        setState(com.scuttlemutt.app.NavActivity.State.SEARCHING)
    }

    override fun onStop() {

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

    // On recieve should have 2 values
    override fun onReceive(endpoint: Endpoint?, payload: Payload?) {
        logD("" + payload?.type)
        if (payload?.type == Payload.Type.BYTES) {
            val buffer = payload?.asBytes()
            val packet = Packet.fromNetworkBytes(buffer)
            val keyPacket = iom.isKeyExchangePacket(packet);
            if(keyPacket != null){
                if(!mutt.haveContact(keyPacket.dawgId.uuid)){
                    logD("NEW CONTACT: " + endpoint?.name)
                    iom.addConnection(keyExchanger.receiveSecretKeyForNewContact(endpoint?.name, keyPacket))
                }else {
                    iom.addConnection(keyExchanger.receiveSecretKey(keyPacket.dawgId, keyPacket))
                }
            }else {
                iom.addReceivedMessage(endpoint?.name, packet)
            }
            var messageString = String(buffer!!)
            if (endpoint != null) {
                messageString = "From id" + endpoint.id + ": " + messageString
                messageString = "From name" + endpoint.name + ": " + messageString
            }
            logI(messageString, false)
            logD("RECIEVED MESSAGE")
        }
    }

    override fun onEndpointDiscovered(endpoint: Endpoint) {
        // We found an advertiser!
        Toast.makeText(this,"Endpoint " + endpoint?.name + " Discovered", Toast.LENGTH_LONG).show()
        logD("Endpoint Discovered")
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
        if (verifyConnection()) {
            acceptConnection(endpoint)
        } else {
            rejectConnection(endpoint)
        }

    }

    override fun onEndpointConnected(endpoint: Endpoint?) {
        logD("CONNECTED to endpoint: " + endpoint?.name)
        Toast.makeText(this,"Connecting to" + endpoint?.id, Toast.LENGTH_LONG).show()
        setState(com.scuttlemutt.app.NavActivity.State.CONNECTED)
        iom.addAvailableConnection(endpoint?.id, endpoint?.name)
        if(mutt.getContactDawgId(endpoint?.name) == null){
            Toast.makeText(this,"Exchanging keys...", Toast.LENGTH_LONG).show()
            keyExchanger.sendKeys(endpoint?.name, mutt.publicKey, mutt.dawgIdentifier);

        }
        Toast.makeText(this,"Ready to talk!", Toast.LENGTH_LONG).show()
    }


    override fun onEndpointDisconnected(endpoint: Endpoint?) {
        Toast.makeText(this,"Endpoints Disconnected", Toast.LENGTH_LONG).show()
        logD("DISCONNECTED")
        setState(com.scuttlemutt.app.NavActivity.State.SEARCHING)
        iom.removeAvailableConnection(endpoint?.name);
    }

    override fun onConnectionFailed(endpoint: Endpoint?) {
        // Let's try someone else.
        Toast.makeText(this,"Connection to endpoint " + endpoint?.name.toString() + " failed", Toast.LENGTH_LONG).show()
        if (getState() == com.scuttlemutt.app.NavActivity.State.SEARCHING) {
            startDiscovering()
        }
    }

    /**
     * The state has changed. Switch it to a new state.
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
                logD("Changed to Searching from " + oldState)
                disconnectFromAllEndpoints()
                iom.removeAllAvailableConnections()
                startDiscovering()
                logD("Start Discovering from state change")
                startAdvertising()
                logD("Start Advertising from state change")
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
