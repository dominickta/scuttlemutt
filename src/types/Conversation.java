package types;

import java.util.List;
import java.util.UUID;

/**
 * Contains the information for a given conversation.
 *
 * NOTE:  This class is currently rather sparse.  We likely still want this class for tracking message conversation
 * metadata though.  In the future, we can add more to this class as necessary.
 */
public class Conversation {
    // class variables
    private final List<DawgIdentifier> userList;
    // private final List<Bark> barks;  // <- TODO:  Add code for this.

    public Conversation(final List<DawgIdentifier> userList) {
        this.userList = userList;
    }

    public List<DawgIdentifier> getUserList() {
        return List.copyOf(userList);
    }

    public List<UUID> getUserUUIDList() {
        return userList.stream()
                .map(DawgIdentifier::getUniqueId)
                .toList();
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