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

    // class variables
    private final DawgIdentifier otherPerson; // whom we are talking to
    private final List<UUID> barkList; // a List of the UUIDs of the Barks associated with the conversation.
                                       // The ordering of the list == the ordering of the barks.

    /**
     * Constructs a Conversation object.
     * 
     * @param otherPerson The DawgIdentifier of the other person we are talking to.
     * @param barkList    A List containing the UUIDs of the Barks in the
     *                    Conversation.
     */
    public Conversation(final DawgIdentifier otherPerson, final List<UUID> barkList) {
        this.otherPerson = otherPerson;
        this.barkList = new ArrayList<UUID>(barkList);
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

    public List<UUID> getBarkUUIDList() {
        return List.copyOf(barkList);
    }

    /**
     * Adds the passed UUID of a Bark to the List of stored Bark UUIDs.
     *
     * NOTE: The database operates via storing serialized representations of the
     * Conversation object. In order to propagate any new Bark UUIDs to the
     * database, we _must_ re-serialize the Conversation objects + overwrite the
     * Conversation object in the database. Simply updating the object + trusting
     * storage-by-reference does _not_ work here.
     *
     * @param barkUUID The UUID being stored.
     */
    public void storeBarkUUID(final UUID barkUUID) {
        this.barkList.add(barkUUID);
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