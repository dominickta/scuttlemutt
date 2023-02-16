package backend.iomanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import types.packet.Packet;

/**
 * Contains static helper methods across IOManager classes.
 */
public class IOManagerHelper {

    /**
     * Obtains the first of the desired Packet type from the passed BlockingQueue.
     *
     * If no such packets of the specified type exist in the BlockingQueue, returns Optional.empty().
     *
     * NOTE:  This method _does not_ empty the BlockingQueue--all Packets which are iterated through + not returned are
     * reloaded upon the queue.
     *
     * @param queue the BlockingQueue which we are polling for the Packet.
     * @param desiredPacketClass the desired class of the Packet.
     * @return an Optional<Packet> containing the first packet of the specified type from the BlockingQueue.  If no such
     * packet of the specified type exists in the queue, returns Optional.empty().
     */
    public static <T extends Packet> Optional<T> getPacketTypeFromBlockingQueue(final BlockingQueue<Packet> queue,
                                                                                final Class<T> desiredPacketClass) {
        // drain the queue's packets to a List.
        final List<Packet> packetList = new ArrayList<Packet>();
        queue.drainTo(packetList);

        // peruse the List for the desired Packet type.
        int packetIdx = 0;
        for (; packetIdx < packetList.size(); packetIdx++) {
            // if the current Packet is of the correct class type, stop the search.
            if (desiredPacketClass.equals(packetList.get(packetIdx).getClass())) {
                break;
            }
        }

        final Optional<T> foundPacketOptional;
        if (packetIdx < packetList.size()) {
            // if we found a packet of the correct type, remove it from the packetList and store it.
            foundPacketOptional = (Optional<T>) Optional.of(packetList.remove(packetIdx));
        } else {
            // otherwise, store that no packet was found.
            foundPacketOptional = Optional.empty();
        }

        // reload all irrelevant Packets onto the queue.
        queue.addAll(packetList);

        // return the foundPacketOptional.
        return foundPacketOptional;
    }
}
