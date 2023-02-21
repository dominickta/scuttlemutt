package backend.meshdaemon;

import backend.iomanager.IOManager;
import storagemanager.StorageManager;
import types.Bark;
import types.Message;
import types.packet.BarkPacket;
import types.Conversation;
import types.DawgIdentifier;

import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Monitors the incoming requests from the IOManager.
 */
public class MeshInput implements Runnable {
    // class variables
    private final IOManager ioManager;
    private final StorageManager storage;
    private final BlockingQueue<Bark> queue;
    private final DawgIdentifier currentUser;

    private final Set<Bark> seenBarks;

    /**
     * Constructs a new MeshInput.
     * 
     * @param ioManager   The underlying IOManager.
     * @param queue       The queue of outgoing barks to forward
     * @param storage     A StorageManager to store Barks addressed to us.
     * @param currentUser  The DawgIdentifier for the current user.
     * @param seenBarks  A Set containing the Barks we have seen before.
     */
    public MeshInput(final IOManager ioManager, final BlockingQueue<Bark> queue,
            final StorageManager storage, final DawgIdentifier currentUser,
            final Set<Bark> seenBarks) {
        this.ioManager = ioManager;
        this.queue = queue;
        this.storage = storage;
        this.currentUser = currentUser;
        this.seenBarks = seenBarks;
    }

    @Override
    public void run() {
        while (true) {
            this.handleInput();
        }
    }

    public void handleInput() {
        // TODO: Add BarkPacket verification (e.g. with signing)

        // Check if ioManager is connected so .call function in Iomanager doesn't fail
        BarkPacket barkPacket = ioManager.meshReceive(BarkPacket.class);
        List<Bark> barkList = barkPacket.getPacketBarks();

        for (Bark bark : barkList) {
            System.out.println("Iterating through messages");
            // if we have seen this bark before, ignore it.
            if (!this.seenBarks.add(bark)) {
                System.out.println("Seen before");
                continue;
            }
            System.out.println("Intended reciever" + bark.getReceiver());
            System.out.println("Me" + this.currentUser);
            if (this.currentUser.equals(bark.getReceiver())) {
                // this is for us!  let's create a plaintext Message object from the Bark + store it
                // for later usage.

                // first, let's extract the contents of the message + the ordering number from the Bark.
                // TODO:  @Justin you may need to modify this section to obtain the ordering number using pubkeys.
                final List<Key> messageContentsKeys = this.storage.lookupKeysForDawgIdentifier(bark.getSender().getUniqueId());
                final Optional<String> messageContents = bark.getContents(messageContentsKeys);

                // if we were unable to successfully decrypt the Bark, toss it out.
                if (!messageContents.isPresent()) {
                    System.out.println("TOSSED OUT");
                    continue;
                }

                // obtain the message ordering num from the Bark.
                // TODO:  @Justin you may need to modify this section to obtain the ordering number using pubkeys.
                final Long messageOrderingNum = bark.getOrderNum();

                // create + store the Message object.
                final Message message = new Message(messageContents.get(), messageOrderingNum, bark.getSender());
                System.out.println("MESSAGE");
                System.out.println(message.getPlaintextMessage());
                storage.storeMessage(message);

                // update the Conversation object stored in the StorageManager to include the Message.
                Conversation c = this.storage.lookupConversation(Collections.singletonList(bark.getSender().getUniqueId()));
                if (c == null) {
                    // if we've never initiated a conversation with the sender before, create + store a new Conversation.
                    c = new Conversation(Collections.singletonList(bark.getSender()),
                            Collections.singletonList(message.getUniqueId()));
                    this.storage.storeConversation(c);
                } else {
                    // update existing obj
                    c.storeMessageUUID(bark.getUniqueId());
                    this.storage.storeConversation(c);
                }

            } else if (!this.currentUser.equals(bark.getSender())) {
                this.queue.add(bark); // put it on output buffer
            }
        }
    }
}
