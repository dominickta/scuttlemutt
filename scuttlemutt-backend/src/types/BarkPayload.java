package types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Stores the metadata for a Bark and represents a crytographic "message" to be
 * signed and encrypted.
 */
public class BarkPayload {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private final String contents;
    private final DawgIdentifier sender;
    private final Long orderNum;

    /**
     * Constructs a new BarkPayload.
     */
    public BarkPayload(final String contents, final DawgIdentifier sender, final Long orderNum) {
        this.contents = contents;
        this.sender = sender;
        this.orderNum = orderNum;
    }

    // public methods
    public String getContents() {
        return this.contents;
    }

    public DawgIdentifier getSender() {
        return this.sender;
    }

    public Long getOrderNum() {
        return this.orderNum;
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BarkPayload)) {
            return false;
        }
        boolean matchContents = this.getContents().equals(((BarkPayload) o).getContents());
        boolean matchSender = this.getSender().equals(((BarkPayload) o).getSender());
        boolean matchOrderNum = this.getOrderNum().equals(((BarkPayload) o).getOrderNum());
        return matchContents && matchSender && matchOrderNum;
    }

    @Override
    public int hashCode() {
        return this.contents.hashCode() * this.sender.hashCode() * this.orderNum.hashCode();
    }

    /**
     * Returns a byte[] containing the bytes which represent the BarkPayload.
     *
     * @return a byte[] containing the bytes which represent the BarkPayload.
     */
    public byte[] toNetworkBytes() {
        return GSON.toJson(this).getBytes();
    }

    /**
     * Returns a BarkPayload derived from the passed byte[].
     *
     * @return a BarkPayload derived from the passed byte[].
     */
    public static BarkPayload fromNetworkBytes(final byte[] bytes) {
        return GSON.fromJson(new String(bytes), BarkPayload.class);
    }

    @Override
    public String toString() {
        return this.sender + "\"" + this.contents + "\"";
    }
}
