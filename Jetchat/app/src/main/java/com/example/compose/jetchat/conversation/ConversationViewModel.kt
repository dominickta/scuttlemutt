package com.example.compose.jetchat.conversation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.compose.jetchat.data.composersMessages
import com.example.compose.jetchat.data.droidconMessages
import com.example.compose.jetchat.data.exampleUiStateComposers

private const val DEFAULT_CHANNEL = "#composers"

class ConversationViewModel : ViewModel() {

    private val _channel = MutableLiveData<String>()
    val channelData: LiveData<String> = _channel

    private val _currUiState = MutableLiveData<ConversationUiState>(exampleUiStateComposers)
    val currUiState: LiveData<ConversationUiState> = _currUiState

    fun setChannel(newChannel: String?) {
        Log.d("ConvViewModel", "Changing channel to: $newChannel")
        val channel = newChannel ?: DEFAULT_CHANNEL

        _channel.value = channel //TODO: remove

        val retChannel = getUiState(channel)
        Log.d("ConvViewModel", "returned channel is ${retChannel.channelName}")
        _currUiState.value = getUiState(channel)
    }

    private fun getUiState(channel: String): ConversationUiState {
        // TODO: This function would become a Room database call instead of if statements
        // TODO: actually, not a database call, but an observable query so that it automatically updates as the database changes

        return if (channel == "#droidcon-nyc") {
            Log.d("ConvViewModel", "returning droidcon")
            ConversationUiState(
                initialMessages = droidconMessages,
                channelName = channel,
                channelMembers = 421
            )
        } else {
            Log.d("ConvViewModel", "returning composers")
            ConversationUiState(
                initialMessages = composersMessages,
                channelName = channel,
                channelMembers = 72
            )
        }
    }

}