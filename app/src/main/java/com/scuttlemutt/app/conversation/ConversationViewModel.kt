import com.scuttlemutt.app.conversation.ConversationUiState
import com.scuttlemutt.app.conversation.FrontEndMessage


import android.util.Log
import androidx.lifecycle.*
import backend.scuttlemutt.Scuttlemutt
import com.scuttlemutt.app.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import types.Bark
import types.DawgIdentifier
import types.Message
import java.util.*
import javax.crypto.SecretKey

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

    // TODO: how will new messages from the database be automatically updated? previously flows were used, currently no way to do this

    init {
//        setChat(initContactName)
//        Log.d(TAG, "My name is ${mutt.dawgIdentifier}")
    }

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
        //Set to default value
        contactID = mutt.dawgIdentifier
        _currUiState.value!!.contactName = contactName
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                if (!mutt.haveContact(mutt.dawgIdentifier.uniqueId)) {
                    Log.d(TAG, "Adding myself because I'm not in the database yet")
                    mutt.addContact(mutt.dawgIdentifier, MyKey) // add myself
                    mutt.sendMessage("talking to myself", mutt.dawgIdentifier)
                }
                if (!mutt.haveContact(ADawgTag.uniqueId)) {
                    Log.d(TAG, "Adding ADawg because not in the database yet")
                    mutt.addContact(ADawgTag, AKey)
                    mutt.sendMessage("hey ADawg, this is me", ADawgTag)
                }
                if (!mutt.haveContact(BDawgTag.uniqueId)) {
                    Log.d(TAG, "Adding BDawg because not in the database yet")
                    mutt.addContact(BDawgTag, BKey)
                    mutt.sendMessage("hey BDawg, this is me", BDawgTag)
                }
                if (!mutt.haveContact(CDawgTag.uniqueId)) {
                    Log.d(TAG, "Adding CDawg because not in the database yet")
                    mutt.addContact(CDawgTag, CKey)
                }
                Log.d(TAG, "Added messages")
                val contactNames: MutableList<String> = mutableListOf()
                for (id in mutt.allContacts) {
                    contactNames.add(id.userContact)
                    if (id.userContact == newChatPartnerName) {
                        contactID = id
                    }
                }
                withContext(Dispatchers.Main) {
                    mainViewModel.setNewContactList(contactNames)
                }
                Log.d(TAG, "beepbeep")

                val conv = mutt.getConversation(Collections.singletonList(contactID))
                if (conv == null) {
                    _currUiState.postValue(ConversationUiState(contactName, listOf()))
                    Log.d(TAG, "Set empty barks for: $contactName")
                } else {
                    var msgs: MutableList<FrontEndMessage> = mutableListOf()
                    val backendMsgs : MutableList<Message>? = mutt.getMessagesForConversation(conv)
                    if (backendMsgs == null) {
                        _currUiState.postValue(ConversationUiState(contactName, listOf()))
                        Log.d(TAG, "Set empty barks for: $contactName")
                    } else {
                        for (m in backendMsgs) {
                            Log.d(TAG, "Message is $m")
                            msgs.add(0,
                                FrontEndMessage(
                                    author = conv.userList.get(0).userContact,
                                    content = m.plaintextMessage,
                                    timestamp = m.orderNum.toString()
                                )
                            )
                        }
                        _currUiState.postValue(ConversationUiState(contactName, msgs))
                        Log.d(TAG, "Set new barks for: $contactName")
                    }
                }
            }
        }
    }

}