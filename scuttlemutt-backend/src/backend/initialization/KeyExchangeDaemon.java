package backend.initialization;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.SecretKey;

import backend.iomanager.IOManager;
import backend.iomanager.IOManagerException;
import crypto.Crypto;
import storagemanager.StorageManager;
import types.DawgIdentifier;
import types.packet.KeyExchangePacket;

/**
 * Background daemon used to exchange keys.
 *
 * When exchangeKeys() is called, public and private keys are exchanged with the specified user.
 */
public class KeyExchangeDaemon {

    public enum KEY_EXCHANGE_STATUS {
        NO_EXCHANGE,
        IN_PROGRESS,
        COMPLETED_SUCCESSFULLY,
        FAILED
    }

    // class variables
    private final IOManager ioManager;
    private final StorageManager storageManager;
    private final PublicKey myPublicKey;  // TODO:  If we ever want to make PublicKeys rotatable,
                                          //   we'll want to edit how they're handled in this class.
    private final DawgIdentifier myDawgId;
    // stores the KeyExchangeDaemonThreads.  we want to store them even after execution completes so
    // that we can verify their status (`COMPLETED_SUCCESSFULLY` or `FAILED`).
    private final Map<String, KeyExchangeDaemonThreadRunnable> threadMap;

    /**
     * Constructs the KeyExchangeDaemon object.
     * @param ioManager  A valid IOManager object.
     * @param storageManager  A valid StorageManager object.
     * @param myPublicKey  This device's PublicKey.
     * @param myDawgId  The DawgIdentifier associated with this device.
     */
    public KeyExchangeDaemon(final IOManager ioManager,
                             final StorageManager storageManager,
                             final PublicKey myPublicKey,
                             final DawgIdentifier myDawgId) {
        this.ioManager = ioManager;
        this.storageManager = storageManager;
        this.myPublicKey = myPublicKey;
        this.myDawgId = myDawgId;
        this.threadMap = new HashMap<String, KeyExchangeDaemonThreadRunnable>();
    }

    /**
     * Spins-up a thread which exchanges keys with the specified user and stores a reference to it.
     *
     * @param otherDeviceId  The ID of the device with which we're exchanging keys.  The ID
     *                       should be understandable to the IOManager.
     */
    public void exchangeKeys(final String otherDeviceId) {
        // verify that there is no exchange currently happening with the other device.
        if (this.threadMap.containsKey(otherDeviceId)
                && KEY_EXCHANGE_STATUS.IN_PROGRESS.equals(this.threadMap.get(otherDeviceId).getCurrentStatus())) {
            throw new RuntimeException("Could not initiate key exchange process with " + otherDeviceId
                    +  " since there is currently an ongoing process with that user!");
        }

        // create the KeyExchangeDaemonThreadRunnable used in the Java thread and store it.
        final KeyExchangeDaemonThreadRunnable threadRunnable
                = new KeyExchangeDaemonThreadRunnable(otherDeviceId);
        this.threadMap.put(otherDeviceId, threadRunnable);

        // spin-up a fresh Thread that executes the KeyExchangeDaemonThreadRunnable.
        final Thread thread = new Thread(threadRunnable);
        thread.start();
    }

    /**
     * Returns the current status for the Key exchange process with the device associated with the
     * specified device ID String.
     * @param otherDeviceId  The ID of the device with which we're looking up the exchange status
     *                       for.  The ID should be understandable to the IOManager.
     * @return  The current status for the Key exchange process with the specified device.
     */
    public KEY_EXCHANGE_STATUS getKeyExchangeStatus(final String otherDeviceId) {
        // if we cannot find any status on-record for the exchange, return NO_EXCHANGE.
        if (!threadMap.containsKey(otherDeviceId)) {
            return KEY_EXCHANGE_STATUS.NO_EXCHANGE;
        }

        // lookup and return the status.
        return threadMap.get(otherDeviceId).getCurrentStatus();
    }

    /**
     * The KeyExchangeDaemon operates by spinning-up, managing, and running KeyExchangeDaemonThreads.
     * KeyExchangeDaemonThreads are where the actual key exchange process occurs.
     */
    private class KeyExchangeDaemonThreadRunnable implements Runnable {
        // class constants
        //
        // the maximum amount of time we will wait (in ms) on the singleDeviceReceive call
        // before timing-out.
        // having this handles timeouts in case the devices get disconnected during the exchange.
        private static final int MAX_RECEIVE_WAIT_MS = 5000;

        // class variables
        private final String otherDeviceId;
        private KEY_EXCHANGE_STATUS currentStatus;

        /**
         * Assembles a KeyExchangeDaemonThread.
         * @param otherDeviceId  The ID of the device with which we're exchanging keys.  The ID
         *                       should be understandable to the IOManager.
         */
        public KeyExchangeDaemonThreadRunnable(final String otherDeviceId) {
            this.otherDeviceId = otherDeviceId;
            this.currentStatus = KEY_EXCHANGE_STATUS.IN_PROGRESS;
        }

        /**
         * Returns the current KEY_EXCHANGE_STATUS of the process being run in this Thread.
         * @return the current KEY_EXCHANGE_STATUS of the process being run in this Thread.
         */
        public KEY_EXCHANGE_STATUS getCurrentStatus() {
            return this.currentStatus;
        }

        @Override
        public void run() {
            // Send a packet.
            final SecretKey localSecretKey = Crypto.generateSecretKey();
            final KeyExchangePacket sentPacket = new KeyExchangePacket(myPublicKey, localSecretKey, myDawgId);
            try {
                ioManager.send(this.otherDeviceId, sentPacket);
            } catch (IOManagerException e) {
                // if we fail to send the KeyExchangePacket packet for some reason, an
                // IOManagerException is thrown.
                //
                // to handle this exception, just set the thread's status to FAILED and exit.
                this.currentStatus = KEY_EXCHANGE_STATUS.FAILED;
                return;
            }

            // Receive a packet.

            // wait for the other device to send us a KeyExchangePacket.
            final KeyExchangePacket receivedPacket;
            try {
                // to avoid timeouts from devices getting disconnected, we spawn a new thread with
                // a time limit when calling the IOManager's singleDeviceReceive() function.
                final ExecutorService service = Executors.newSingleThreadExecutor();
                final Callable<KeyExchangePacket> keCallable = new Callable<KeyExchangePacket>() {
                    @Override
                    public KeyExchangePacket call() {
                        return ioManager.singleDeviceReceive(otherDeviceId, KeyExchangePacket.class);
                    }
                };
                final Future<KeyExchangePacket> keyExchangePacketFuture = service.submit(keCallable);
                // attempt to retrieve the packet from the Future object.  if the future object
                // fails to complete execution before the timeout, a TimeoutException is thrown.
                receivedPacket = keyExchangePacketFuture.get(MAX_RECEIVE_WAIT_MS, TimeUnit.MILLISECONDS);
            }
            catch (final InterruptedException | TimeoutException | ExecutionException e) {
                // If the thread was interrupted, took too long, or had an error,
                // set the status to FAILED and exit.
                this.currentStatus = KEY_EXCHANGE_STATUS.FAILED;
                return;
            }

            // extract the Keys + DawgIdentifier from the packet.
            final PublicKey otherPublicKey = receivedPacket.getPublicKey();
            final SecretKey otherSecretKey = receivedPacket.getSecretKey();
            final DawgIdentifier otherDawgId = receivedPacket.getDawgId();

            // if we've never seen the sender before, store their DawgIdentifier in the StorageManager.
            if (storageManager.lookupDawgIdentifierForUuid(otherDawgId.getUUID()) == null) {
                storageManager.storeDawgIdentifier(otherDawgId);
            }

            // at this point, we have keys from both parties. let's determine which one
            // should be used for the connections by hashing them and choosing the one with
            // the higher-value.
            final SecretKey chosenKey = localSecretKey.hashCode() < otherSecretKey.hashCode() ? otherSecretKey : localSecretKey;

            // store the Keys.
            storageManager.storeSecretKeyForUUID(otherDawgId.getUUID(), chosenKey);
            storageManager.storePublicKeyForUUID(otherDawgId.getUUID(), otherPublicKey);

            // update the Thread's status to indicate that it completed successfully!
            this.currentStatus = KEY_EXCHANGE_STATUS.COMPLETED_SUCCESSFULLY;
        }
    }
}
