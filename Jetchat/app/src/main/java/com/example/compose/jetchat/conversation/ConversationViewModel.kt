package com.example.compose.jetchat.conversation

import android.util.Log
import androidx.lifecycle.*
import com.example.compose.jetchat.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
        ConversationUiState(contactName = initContactName, messages = listOf()))
    val currUiState: LiveData<ConversationUiState> = _currUiState

    var barkUpdater : Job? = null

    init {
        setChat(initContactName)
    }

    fun addMessage(msg: String) {
        viewModelScope.launch {
            val dstPubKey = database.contactDao().getContactByNickname(_contactName.value!!).publicKey
            val lastSeqNum = database.barkDao().getLastSeqNum(srcPublicKey = "myPublicKey", dstPublicKey = dstPubKey) ?: 0
            Log.d(TAG, "lastSeqNum is $lastSeqNum, dstPubKey is $dstPubKey")
            database.barkDao().insert(Bark(srcPublicKey = "myPublicKey", dstPublicKey = dstPubKey, timestamp = SimpleDateFormat(
                "MM/dd/yyyy hh.mm aa",
                Locale.getDefault()
            ).format(Date()), seqNum = lastSeqNum + 1, msg = msg))
            _currUiState.value
        }
    }

    fun setChat(contactName: String) {
        Log.d(TAG, "Changing contact to: $contactName")
        _contactName.value = contactName
        _currUiState.value!!.contactName = contactName
        if (barkUpdater != null) {
            barkUpdater!!.cancel()
        }
        barkUpdater = viewModelScope.launch {
            while (isActive) {
                val con : Contact = database.contactDao().getContactByNickname(contactName)
                if (con == null) {
                    // it is possible for con to be null
                    continue
                }
                Log.d(TAG, "con.publickey is: ${con.publicKey}")
                database.barkDao().getBarks(srcPublicKey = "myPublicKey", dstPublicKey = con.publicKey).cancellable().collect {
                    Log.d(TAG, "Got some new barks for: $contactName")
                    var msgs : MutableList<FrontEndMessage> = mutableListOf()
                    for (bark in it) {
                        val author = database.contactDao().getContactByKey(bark.srcPublicKey).nickname
                        msgs.add(FrontEndMessage(
                            author = author,
                            content = bark.msg,
                            timestamp = bark.timestamp
                        ))
                    }
                    // Below is bad example of updating livedata, and does not cause composables to recompose
//                    _currUiState.value!!.messages = msgs
//                    Log.d(TAG, "Set new barks for: $contactName")
                    _currUiState.postValue(ConversationUiState(contactName, msgs))
                    Log.d(TAG, "Set new barks for (try 2): $contactName")
                }
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