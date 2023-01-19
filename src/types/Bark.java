package types;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a "bark" (message) sent by the user.
 */
public class Bark implements Serializable {
    // constants

    // stores the maximum number of characters allowed in a "Bark".
    public static final int MAX_MESSAGE_SIZE = 160;

    // class variables
    private final String contents;
    private final UUID uniqueId;

    /**
     * Constructs a new Bark.
     * @param contents  The contents of the message.
     */
    public Bark(final String contents) {

        // verify that the contents are less than the max message size.
        if (contents.length() > MAX_MESSAGE_SIZE) {
            throw new RuntimeException("Attempted to create a Bark object with a message larger than the maximum size!" +
                    "\tBark message length:  " + contents.length() +"\tMaximum size:  " + MAX_MESSAGE_SIZE);
        }

        this.contents = contents;
        this.uniqueId = UUID.randomUUID();
    }

    // public methods

    public String getContents() {
        return this.contents;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Bark)) {
            return false;
        }
        return this.getUniqueId().equals(((Bark) o).getUniqueId());
    }

    @Override
    public String toString() {
        return "Contents:  " + this.contents + "\tuniqueId:  " + this.uniqueId.toString();
    }
}