package types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Contains the information for a given conversation.
 *
 * NOTE:  This class is currently rather sparse.  We likely still want this class for tracking message conversation
 * metadata though.  In the future, we can add more to this class as necessary.
 */
public class Conversation {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // class variables
    private final List<DawgIdentifier> userList;
    private final List<UUID> messageList;  // a List of the UUIDs of the Messages associated with the conversation.
                                           // The ordering of the list == the ordering of the Messages.


    /**
     * Constructs a Conversation object.
     * @param userList  A List specifying the users involved in the Conversation.
     * @param messageList  A List containing the UUIDs of the Messages in the Conversation.
     */
    public Conversation(final List<DawgIdentifier> userList, final List<UUID> messageList) {
        this.userList = userList;
        this.messageList = new ArrayList<UUID>(messageList);
    }

    /**
     * Second constructor for when no Messages exist yet.
     * @param userList  A List specifying the users involved in the Conversation.
     */
    public Conversation(final List<DawgIdentifier> userList) {
        this(userList, new ArrayList<UUID>());
    }

    public List<DawgIdentifier> getUserList() {
        return List.copyOf(userList);
    }

    public List<UUID> getUserUUIDList() {
        return userList.stream()
                .map(DawgIdentifier::getUniqueId)
                .collect(Collectors.toList());
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
        return this.getUserList().equals(((Conversation) o).getUserList());
    }

    @Override
    public String toString() {
        return userList.toString();
    }
}