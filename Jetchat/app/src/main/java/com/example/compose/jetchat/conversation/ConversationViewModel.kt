package com.example.compose.jetchat.conversation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.ColumnInfo
import com.example.compose.jetchat.data.*
import kotlinx.coroutines.flow.Flow

private const val DEFAULT_CHANNEL = "#composers"

class ConversationViewModelFactory (private val database: ScuttlemuttDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            return ConversationViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class ConversationViewModel(private val database: ScuttlemuttDatabase) : ViewModel() {

    private val _channel = MutableLiveData<String>()
    val channelData: LiveData<String> = _channel

    private val _currUiState = MutableLiveData<ConversationUiState>(exampleUiStateComposers)
    val currUiState: LiveData<ConversationUiState> = _currUiState

    suspend fun addMessage(msg: String) {
        Log.d("ConversationViewModel", "Adding $msg to database")
        val lastSeqNum = database.barkDao().getLastSeqNum("testKey") ?: 0
        database.barkDao().insert(Bark(publicKey = "testKey", seqNum = lastSeqNum + 1, msg = msg))
    }

    fun setChat(contactName: String) {

    }

    fun setChannel(newChannel: String?) {
        Log.d("ConvViewModel", "Changing channel to: $newChannel")
        val channel = newChannel ?: DEFAULT_CHANNEL

        _channel.value = channel //TODO: remove

        val retChannel = getUiState(channel)
        Log.d("ConvViewModel", "returned channel is ${retChannel.channelName}")
        _currUiState.value = getUiState(channel)
//                        https://developer.android.com/codelabs/basic-android-kotlin-training-intro-room-flow#8
    }

    private fun getUiState(contactName: String) : ConversationUiState {
        val con : Contact = database.contactDao().getContactByNickname(contactName)
        val barks : Flow<List<Bark>> = database.barkDao().getBarks(con.publicKey)
        val uiState: ConversationUiState = ConversationUiState(
            initialMessages = ,
            channelName = channel,
            channelMembers = 421
        )

    }

//    private fun getUiState(channel: String): ConversationUiState {
//        // TODO: This function would become a Room database call instead of if statements
//        // TODO: actually, not a database call, but an observable query so that it automatically updates as the database changes
//
//        return if (channel == "#droidcon-nyc") {
//            Log.d("ConvViewModel", "returning droidcon")
//            ConversationUiState(
//                initialMessages = droidconMessages,
//                channelName = channel,
//                channelMembers = 421
//            )
//        } else {
//            Log.d("ConvViewModel", "returning composers")
//            ConversationUiState(
//                initialMessages = composersMessages,
//                channelName = channel,
//                channelMembers = 72
//            )
//        }
//    }

}