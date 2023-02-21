package backend.meshdaemon;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import javax.crypto.SecretKey;

import backend.iomanager.IOManager;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;
import types.packet.BarkPacket;

/**
 * Monitors the incoming requests from the IOManager.
 */
public class MeshInput implements Runnable {
    // class variables
    private final IOManager ioManager;
    private final StorageManager storage;
    private final BlockingQueue<Bark> queue;
    private final PrivateKey myPrivateKey;
    private final Set<Bark> seenBarks;

    /**
     * Constructs a new MeshInput.
     * 
     * @param ioManager   The underlying IOManager.
     * @param queue       The queue of outgoing barks to forward
     * @param storage     A StorageManager to store Barks addressed to us.
     * @param currentUser The DawgIdentifier for the current user.
     * @param seenBarks   A Set containing the Barks we have seen before.
     */
    public MeshInput(final IOManager ioManager, final BlockingQueue<Bark> queue,
            final StorageManager storage, final PrivateKey myPrivateKey,
            final Set<Bark> seenBarks) {
        this.ioManager = ioManager;
        this.queue = queue;
        this.storage = storage;
        this.myPrivateKey = myPrivateKey;
        this.seenBarks = seenBarks;
    }

    @Override
    public void run() {
        while (true) {
            this.handleInput();
        }
    }

    public void handleInput() {
        // TODO: Add BarkPacket verification (e.g. crypto signatures)

        // Check if ioManager is connected so .call function in Iomanager doesn't fail
        BarkPacket barkPacket = ioManager.meshReceive(BarkPacket.class);
        List<Bark> barkList = barkPacket.getPacketBarks();

        for (Bark bark : barkList) {
            // if we have seen this bark before, ignore it.
            if (!this.seenBarks.add(bark)) {
                continue;
            }
            if (bark.isForMe(this.myPrivateKey)) {
                // this is for us! let's create a plaintext Message object from
                // the Bark and store it for later usage.
                storage.storeBark(bark); // TODO @John: can i delete this line?

                // first, let's extract the contents of the message and the
                // ordering number from the bark
                final UUID senderId = bark.getSenderUUID(myPrivateKey);
                final List<SecretKey> secretKeys = this.storage.lookupSecretKeysForUUID(senderId);
                final DawgIdentifier sender = bark.getSender(secretKeys);
                final String messageContents = bark.getContents(secretKeys);

                // obtain the message ordering num from the Bark.
                final Long messageOrderingNum = bark.getOrderNum(secretKeys);

                // create + store the Message object.
                final Message message = new Message(messageContents, messageOrderingNum, sender);
                storage.storeMessage(message);

                // update the Conversation stored in the StorageManager to include the Message.
                Conversation c = this.storage.lookupConversation(senderId);
                if (c == null) {
                    // if we've never initiated a conversation with the sender before, create +
                    // store a new Conversation.
                    c = new Conversation(sender, Collections.singletonList(message.getUniqueId()));
                    this.storage.storeConversation(c);
                } else {
                    // update existing obj
                    c.storeMessageUUID(message.getUniqueId());
                    this.storage.storeConversation(c);
                }
            } else {
                this.queue.add(bark); // put it on output buffer
            }
        }
    }
}
