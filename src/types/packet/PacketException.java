package types.packet;

public class PacketException extends RuntimeException {
    public PacketException(String errorMessage) {
        super(errorMessage);
    }
}
