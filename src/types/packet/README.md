Contains the Packet abstract class + classes for all Packet types:
- `Packet.java`:  the abstract class for all Packet types.  All packet types _must_ extend this class to be used with the IOManager.
- `BarkPacket.java`:  the Packet class used for sending/receiving Barks.
- `KeyExchangePacket.java`:  the Packet class used for PublicKey exchange.
- `PacketException.java`:  The standard `RuntimeException`-type for Packet-related issues.