import com.scuttlemutt.app.conversation.ConversationUiState
import com.scuttlemutt.app.conversation.FrontEndMessage


import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import backend.iomanager.IOManagerException
import backend.scuttlemutt.Scuttlemutt
import com.scuttlemutt.app.*
import kotlinx.coroutines.*
import types.DawgIdentifier
import types.Message
import java.util.*

class ConversationViewModelFactory (private val mainViewModel: MainViewModel, private val mutt: Scuttlemutt, private val initContactName: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            return ConversationViewModel(mainViewModel, mutt, initContactName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ConversationViewModel(private val mainViewModel: MainViewModel, private val mutt: Scuttlemutt, initContactName: String) : ViewModel() {

    private val TAG = "ConversationViewModel"

    // Data about the current open chat partner
    private var contactName: String = initContactName
    private lateinit var contactID: DawgIdentifier

    // UI View
    private val _currUiState = MutableLiveData(ConversationUiState(contactName = contactName, messages = listOf()))
    val currUiState: LiveData<ConversationUiState> = _currUiState

    // Job that loops to retrieve new barks
    var barkUpdater : Job? = null

    fun addMessage(msg: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                mutt.sendMessage(msg, contactID)
                var msgs: MutableList<FrontEndMessage> = _currUiState.value!!.messages.toMutableList()
                msgs.add(0, FrontEndMessage("me", msg, "0"))
                _currUiState.postValue(ConversationUiState(contactName, msgs))
            }
        }
    }

    fun setChat(newChatPartnerName: String) {
        Log.d(TAG, "Changing contact to: $newChatPartnerName")
        contactName = newChatPartnerName
        // Automatically set the current conversation to ourselves
        contactID = mutt.dawgIdentifier
        _currUiState.value!!.contactName = contactName
        _currUiState.postValue(ConversationUiState(contactName, listOf()))

        barkUpdater?.cancel() // ? is a Kotlin safe call
        barkUpdater = viewModelScope.launch(Dispatchers.Default) {
            Log.d(TAG, "Starting barkupdater")
            initContacts()
            Log.d(TAG, "looping contacts")
            val contactNames: MutableList<String> = mutableListOf()
            for (id in mutt.allContacts) {
                contactNames.add(id.username)
                if (id.username == newChatPartnerName) {
                    contactID = id
                }
            }
            Log.d(TAG, "starting job")
            while (coroutineContext.isActive) {
                val conv = mutt.getConversation(contactID)
                if (conv == null) {
                    if (_currUiState.value!!.messages.isNotEmpty()) {
                        _currUiState.postValue(ConversationUiState(contactName, listOf()))
                        Log.d(TAG, "Set empty barks for: $contactName")
                    }
                    continue
                }
                var msgs: MutableList<FrontEndMessage> = mutableListOf()
                val backendMsgs : MutableList<Message>? = mutt.getMessagesForConversation(conv)
                if (backendMsgs == null) {
                    if (_currUiState.value!!.messages.isNotEmpty()) {
                        _currUiState.postValue(ConversationUiState(contactName, listOf()))
                        Log.d(TAG, "Set empty barks for: $contactName")
                    }
                    continue
                }
                for (m in backendMsgs) {
//                    Log.d(TAG, "Message is $m")
                    msgs.add(0,
                        FrontEndMessage(
                            author = conv.otherPerson.username,
                            content = m.plaintextMessage,
                            timestamp = m.orderNum.toString()
                        )
                    )
                }
                if (_currUiState.value!!.messages != msgs) {
                    _currUiState.postValue(ConversationUiState(contactName, msgs))
                    Log.d(TAG, "Set new barks for: $contactName")
                }
            }
        }
    }
    private fun initContacts() {
        if (!mutt.haveContact(mutt.dawgIdentifier.uuid)) {
            Log.d(TAG, "Adding myself because I'm not in the database yet")
            mutt.addContact(mutt.dawgIdentifier, MyKeyPair.public, MyKey) // add myself
            mutt.sendMessage("talking to myself", mutt.dawgIdentifier)
        }
        if (!mutt.haveContact(ADawgTag.uuid)) {
            Log.d(TAG, "Adding ADawg because not in the database yet")
            mutt.addContact(ADawgTag, AKeyPair.public, AKey)
            mutt.sendMessage("hey ADawg, this is me", ADawgTag)
        }
        if (!mutt.haveContact(BDawgTag.uuid)) {
            Log.d(TAG, "Adding BDawg because not in the database yet")
            mutt.addContact(BDawgTag, BKeyPair.public, BKey)
            mutt.sendMessage("hey BDawg, this is me", BDawgTag)
        }
        if (!mutt.haveContact(CDawgTag.uuid)) {
            Log.d(TAG, "Adding CDawg because not in the database yet")
            mutt.addContact(CDawgTag, CKeyPair.public, CKey)
        }
    }

}