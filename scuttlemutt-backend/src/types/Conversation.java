package types;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Contains the information for a given conversation.
 *
 * NOTE: This class is currently rather sparse. We likely still want this class
 * for tracking message conversation
 * metadata though. In the future, we can add more to this class as necessary.
 */
public class Conversation {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // Whom we are talking to.
    private final DawgIdentifier otherPerson;
    
    // A List of the UUIDs of the Messages associated with the conversation.
    // The ordering of the list == the ordering of the Messages.
    private final List<UUID> messageList;
    
    /**
     * Constructs a Conversation object.
     * 
     * @param otherPerson The DawgIdentifier of the other person we are talking to.
     * @param messageList  A List containing the UUIDs of the Messages in the Conversation.
     */
    public Conversation(final DawgIdentifier otherPerson, final List<UUID> messageList) {
        this.otherPerson = otherPerson;
        this.messageList = new ArrayList<UUID>(messageList);
    }
    
    /**
     * Second constructor for when no Barks exist yet.
     * 
     * @param otherPerson Whom we are talking to.
     */
    public Conversation(final DawgIdentifier otherPerson) {
        this(otherPerson, new ArrayList<UUID>());
    }

    public DawgIdentifier getOtherPerson() {
        return this.otherPerson;
    }

    public List<UUID> getMessageUUIDList() {
        return List.copyOf(messageList);
    }

    /**
     * Adds the passed UUID of a Message to the List of stored Message UUIDs.
     *
     * NOTE:  The database operates via storing serialized representations of the Conversation object.  In order to
     * propagate any new Message UUIDs to the database, we _must_ re-serialize the Conversation objects + overwrite the
     * Conversation object in the database.  Simply updating the object + trusting storage-by-reference does _not_ work
     * here.
     *
     * @param messageUUID  The UUID being stored.
     */
    public void storeMessageUUID(final UUID messageUUID) {
        this.messageList.add(messageUUID);
    }

    /**
     * Returns a byte[] containing the bytes which represent the Conversation.
     *
     * @return a byte[] containing the bytes which represent the Conversation.
     */
    public byte[] toNetworkBytes() {
        return GSON.toJson(this).getBytes();
    }

    /**
     * Returns a Conversation derived from the passed byte[].
     *
     * @return a Conversation derived from the passed byte[].
     */
    public static Conversation fromNetworkBytes(final byte[] conversationBytes) {
        return GSON.fromJson(new String(conversationBytes), Conversation.class);
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Conversation)) {
            return false;
        }
        return this.getOtherPerson().equals(((Conversation) o).getOtherPerson());
    }

    @Override
    public String toString() {
        return "A conversation between me and " + otherPerson.toString();
    }
}