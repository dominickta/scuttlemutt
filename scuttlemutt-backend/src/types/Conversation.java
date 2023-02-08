package types;

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
    // class variables
    private final List<DawgIdentifier> userList;
    private final List<UUID> barkList;  // a List of the UUIDs of the Barks associated with the conversation.
                                        // The ordering of the list == the ordering of the barks.


    /**
     * Constructs a Conversation object.
     * @param userList  A List specifying the users involved in the Conversation.
     * @param barkList  A List containing the UUIDs of the Barks in the Conversation.
     */
    public Conversation(final List<DawgIdentifier> userList, final List<UUID> barkList) {
        this.userList = userList;
        this.barkList = new ArrayList<UUID>(barkList);
    }

    /**
     * Second constructor for when no Barks exist yet.
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

    public List<UUID> getBarkUUIDList() {
        return List.copyOf(barkList);
    }

    /**
     * Adds the passed UUID of a Bark to the List of stored Bark UUIDs.
     *
     * NOTE:  The database operates via storing serialized representations of the Conversation object.  In order to
     * propagate any new Bark UUIDs to the database, we _must_ re-serialize the Conversation objects + overwrite the
     * Conversation object in the database.  Simply updating the object + trusting storage-by-reference does _not_ work
     * here.
     *
     * @param barkUUID  The UUID being stored.
     */
    public void storeBarkUUID(final UUID barkUUID) {
        this.barkList.add(barkUUID);
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