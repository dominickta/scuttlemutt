package com.example.compose.jetchat.conversation

import android.util.Log
import androidx.lifecycle.*
import androidx.room.ColumnInfo
import com.example.compose.jetchat.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val DEFAULT_CHANNEL = "#composers"

class ConversationViewModelFactory (private val database: ScuttlemuttDatabase, private val initContactName: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            return ConversationViewModel(database, initContactName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// TODO: ViewModel needs to convert barks into messages for the frontend that uses nicknames
class ConversationViewModel(private val database: ScuttlemuttDatabase, private val initContactName: String) : ViewModel() {

    private val TAG = "ConversationViewModel"

    private val _contactName = MutableLiveData<String>(initContactName)
    val contactName: LiveData<String> = _contactName

    private val _channel = MutableLiveData<String>()
    val channelData: LiveData<String> = _channel

    private val _currUiState = MutableLiveData<ConversationUiState>(
        ConversationUiState(contactName = initContactName, initialMessages = listOf()))
    val currUiState: LiveData<ConversationUiState> = _currUiState

    var barkUpdater : Job? = null

    init {
        setChat(initContactName)
    }

    fun addMessage(msg: String) {
        Log.d("ConversationViewModel", "Adding $msg to database")
        val lastSeqNum = database.barkDao().getLastSeqNum("testKey") ?: 0
        viewModelScope.launch {
            database.barkDao().insert(Bark(srcPublicKey = "myPublicKey", dstPublicKey = "testDstPublicKey", seqNum = lastSeqNum + 1, msg = msg))
        }
    }

    fun setChat(contactName: String) {
        Log.d(TAG, "Changing contact to: $contactName")
        _contactName.value = contactName
        val con : Contact = database.contactDao().getContactByNickname(contactName)
        if (barkUpdater != null) {
            barkUpdater!!.cancel()
        }
        barkUpdater = viewModelScope.launch {
            // Coroutine that will be canceled when the ViewModel is cleared.
            database.barkDao().getBarks(con.publicKey).cancellable().collect {
                currUiState.value!!.messages = it
            }
        }
    }

//    fun setChannel(newChannel: String?) {
//        Log.d("ConvViewModel", "Changing channel to: $newChannel")
//        val channel = newChannel ?: DEFAULT_CHANNEL
//
//        _channel.value = channel //TODO: remove
//
//        val retChannel = getUiState(channel)
//        Log.d("ConvViewModel", "returned channel is ${retChannel.channelName}")
//        _currUiState.value = getUiState(channel)
////                        https://developer.android.com/codelabs/basic-android-kotlin-training-intro-room-flow#8
//    }

//    private fun getUiState(contactName: String) : ConversationUiState {
////        val barks : Flow<List<Bark>> = database.barkDao().getBarks(con.publicKey)
//
//
//
//
//    }

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