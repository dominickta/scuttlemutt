package types;

import java.util.List;

/**
 * Contains the information for a given conversation.
 *
 * NOTE:  This class is currently rather sparse.  We likely still want this class for tracking message conversation
 * metadata though.  In the future, we can add more to this class as necessary.
 */
public class ConversationInfo {
    // class variables
    private final List<MuttIdentifier> userList;

    public ConversationInfo(final List<MuttIdentifier> userList) {
        this.userList = userList;
    }

    public List<MuttIdentifier> getUserList() {
        return List.copyOf(userList);
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConversationInfo)) {
            return false;
        }
        return this.getUserList().equals(((ConversationInfo) o).getUserList());
    }

    @Override
    public String toString() {
        return userList.toString();
    }
}