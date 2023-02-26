package backend.meshdaemon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import backend.iomanager.QueueIOManager;
import crypto.Crypto;
import storagemanager.MapStorageManager;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;
import types.TestUtils;
import types.packet.Packet;

public class MeshDaemonTest {
        // test objects
        private StorageManager storageManager;
        private DawgIdentifier userDawgId, // DawgIdentifier of the device used by MeshDaemon.
                        otherDawgId; // DawgIdentifier to which the MeshDaemon is sending messages.
        private QueueIOManager ioManager;
        private MeshDaemon meshDaemon;
        private SecretKey conversationKey;
        private BlockingQueue<Bark> meshDaemonInternalBarkQueue; // the MeshDaemon's internal queue
                                                                 // used for sending Barks.

        @BeforeEach
        public void setup() {
                // create the MeshDaemon object.
                this.storageManager = new MapStorageManager();
                this.userDawgId = TestUtils.generateRandomizedDawgIdentifier();
                this.ioManager = new QueueIOManager();
                this.meshDaemon = new MeshDaemon(this.ioManager, this.storageManager, this.userDawgId);

                // setup a fake "connection" with another device which we can monitor for
                // messages.
                this.otherDawgId = TestUtils.generateRandomizedDawgIdentifier();
                this.ioManager.connect(this.otherDawgId.getUsername(),
                                new LinkedBlockingQueue<Packet>(),
                                new LinkedBlockingQueue<Packet>());

                // store a SymmetricKey for the fake "connection".
                this.storageManager.storePublicKeyForUUID(otherDawgId.getUUID(), Crypto.ALICE_KEYPAIR.getPublic());
                this.storageManager.storePrivateKey(Crypto.ALICE_KEYPAIR.getPrivate());
                this.conversationKey = Crypto.DUMMY_SECRETKEY;
                this.storageManager.storeSecretKeyForUUID(this.otherDawgId.getUUID(),
                                this.conversationKey);

                // get a reference to the MeshDaemon's internal Bark queue.
                this.meshDaemonInternalBarkQueue = Whitebox.getInternalState(this.meshDaemon, "queue");
        }

        @Test
        public void testSendMessage_storesMessageAndConvoAndBarkInDb_sendsMessageSuccessfully() {
                // create the message being sent.
                final String messageContents = RandomStringUtils.randomAlphanumeric(15);
                final Long seqId = new Random().nextLong();

                // send the message.
                this.meshDaemon.sendMessage(messageContents, this.otherDawgId, seqId);

                // verify that the MeshDaemon handled the message as expected.

                // check that the Bark queue was fed a Bark object containing the message.
                final Bark sentBark = this.meshDaemonInternalBarkQueue.remove();
                final String decryptedMessage = sentBark.getContents(List.of(this.conversationKey), Crypto.ALICE_KEYPAIR.getPublic());
                assertEquals(messageContents, decryptedMessage);

                // verify that the StorageManager stored the data for a Bark.
                final Bark storedBark = this.storageManager.lookupBark(sentBark.getUniqueId());
                assertEquals(sentBark, storedBark);

                // verify that the StorageManager stored the data for a Conversation involving a
                // message.
                // NOTE: Since no preexisting Conversation was present, just having one exist
                // with this.otherDawgId
                // means that the Conversation content for the message was processed.
                final Conversation storedConversation = this.storageManager
                                .lookupConversation(this.otherDawgId.getUUID());
                assertNotNull(storedConversation);

                // the Conversation object should contain one Message UUID, and the
                // corresponding Message
                // object should contain the expected messageContents and seqId.
                assertEquals(1, storedConversation.getMessageUUIDList().size());
                final Message message = this.storageManager
                                .lookupMessage(storedConversation.getMessageUUIDList().get(0));
                assertEquals(messageContents, message.getPlaintextMessage());
                assertEquals(seqId, message.getOrderNum());
        }
}
