package storagemanager;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;

/**
 * This interface specifies how the StorageManager objects should be written.
 */
public interface StorageManager {
    /**
     * The maximum number of keys which should be stored for a particular
     * dawgidentifier. Once we store more than this value, we will remove the
     * oldest stored key for the id from the db.
     */
    public static final int MAX_NUM_HISTORICAL_KEYS_TO_STORE = 5;

    //////////////////
    // lookup* methods
    //////////////////

    /**
     * Returns the Bark object associated with the given bark id.
     *
     * @param barkUuid the UUID of the bark to look for
     * @return the Bark object or null if not found
     */
    Bark lookupBark(final UUID barkUuid);

    /**
     * Returns the DawgIdentifier object associated with the given UUID.
     *
     * @param id the UUID of the DawgIdentifier to look for
     * @return the DawgIdentifier object or null if not found
     */
    DawgIdentifier lookupDawgIdentifierForUuid(final UUID id);

    /**
     * Returns the DawgIdentifier object associated with the given username.
     *
     * @param username the username String identifying the DawgIdentifier to look for
     * @return the DawgIdentifier object or null if not found
     */
    DawgIdentifier lookupDawgIdentifierForUsername(final String username);

    /**
     * Returns the Conversation object associated with the given id.
     *
     * @param id the UUID of the conversation to look for
     * @return the Conversation object or null if not found
     */
    Conversation lookupConversation(final UUID id);

    /**
     * Returns the PublicKey associated with the given dawgIdentifier's id.
     *
     * @param id the UUID of the DawgIdentifier to look for
     * @return the PublicKey or null if not found
     */
    PublicKey lookupPublicKeyForUUID(final UUID id);

    /**
     * Returns the list of secret keys associated with the given dawgIdentifier
     * id. The list of keys is in chronological order of keys being added with
     * the most recent keys at the end and the oldest keys at the front.
     *
     * NOTE: The size is never more than `MAX_NUM_HISTORICAL_KEYS_TO_STORE`.
     *
     * @param id the UUID of the DawgIdentifier to look for
     * @return the list of secret keys or null if not found
     */
    List<SecretKey> lookupSecretKeysForUUID(final UUID id);

    /**
     * Returns the Message associated with the given dawgIdentifier's id.
     *
     * @param id the UUID of the DawgIdentifier to look for
     * @return the Message or null if not found
     */
    Message lookupMessage(final UUID id);

    /**
     * @return The private key of the current user.
     */
    PrivateKey lookupPrivateKey();

    /**
     * A convenience method for looking-up latest SecretKey for a particular
     * user's DawgIdentifier.
     *
     * @param id the UUID of the DawgIdentifier to look for
     * @return the most recently added SecretKey or null
     */
    default SecretKey lookupLatestSecretKeyForDawgIdentifier(final UUID id) {
        List<SecretKey> keys = this.lookupSecretKeysForUUID(id);
        return keys.get(keys.size() - 1);
    }

    /////////////////
    // store* methods
    /////////////////

    /**
     * Stores the given Bark object, index by its unique id.
     *
     * Note: If there are any preexisting objects, they are overwritten.
     *
     * @param bark the bark to store
     */
    void storeBark(final Bark bark);

    /**
     * Stores the given DawgIdentifier object, index by its unique id.
     *
     * Note: If there are any preexisting objects, they are overwritten.
     *
     * @param dawgIdentifier the identifier to store
     */
    void storeDawgIdentifier(final DawgIdentifier dawgIdentifier);

    /**
     * Stores the given Conversation object, index by its unique id.
     *
     * Note: If there are any preexisting objects, they are overwritten.
     *
     * @param conversation the identifier to store
     */
    void storeConversation(final Conversation conversation);

    /**
     * Associates the user with a DawgIdentifier that has a unique id that
     * matches the given id with the supplied SecretKey.
     *
     * Since we can store more than one SecretKey for each DawgIdentifier, this
     * method will append this key to the end of that list. If the resulting
     * list would be longer than `MAX_NUM_HISTORICAL_KEYS_TO_STORE` then the
     * oldest key is removed.
     *
     * @param id  the UUID of the dawgIdentifier to store the secret for
     * @param key the secret key to store
     */
    void storeSecretKeyForUUID(final UUID id, final SecretKey key);

    /**
     * Associates the user with a DawgIdentifier that has a unique id that
     * matches the given id with the supplied PublicKey.
     *
     * This operation will overwrite any existing PublicKey stored with the
     * given device id.
     *
     * @param id  the UUID of the dawgIdentifier to store the public key for
     * @param key the PublicKey to store
     */
    void storePublicKeyForUUID(final UUID id, final PublicKey key);

    /**
     * Stores the current user's private key. Will overwrite any private keys
     * that were previously stored.
     *
     * @param privateKey the PrivateKey to store for the current user
     */
    void storePrivateKey(final PrivateKey privateKey);

    /**
     * Stores the given Message object, index by its unique id.
     *
     * If there are any preexisting objects, they are overwritten.
     *
     * @param message the Message to store
     */
    void storeMessage(final Message message);

    //////////////////
    // delete* methods
    //////////////////

    /**
     * Removes the Bark with the associated unique id.
     *
     * @param id the UUID of the bark to delete
     * @return the Bark which was deleted, otherwise returns null
     */
    Bark deleteBark(final UUID id);

    /**
     * Removes the DawgIdentifier with the associated unique id.
     *
     * @param id the UUID of the DawgIdentifier to delete
     * @return the DawgIdentifier which was deleted, otherwise returns null
     */
    DawgIdentifier deleteDawgIdentifierByUuid(final UUID id);

    /**
     * Removes the DawgIdentifier with the associated username String.
     *
     * @param username the username String of the DawgIdentifier to delete
     * @return the DawgIdentifier which was deleted, otherwise returns null
     */
    DawgIdentifier deleteDawgIdentifierByUsername(final String username);

    /**
     * Removes the Conversation with the associated unique id.
     *
     * @param id the UUID of the Conversation to delete
     * @return the Conversation which was deleted, otherwise returns null
     */
    Conversation deleteConversation(final UUID id);

    /**
     * Removes the PublicKey associated with the given dawgIdentifier uuid.
     *
     * @param id the UUID of the DawgIdentifier whose public key we are removing
     * @return the PublicKey which was deleted, otherwise returns null
     */
    PublicKey deletePublicKeyForUUID(final UUID id);

    /**
     * Removes all SecretKeys associated with the given dawgIdentifier uuid.
     *
     * @param id the UUID of the DawgIdentifier whose secret keys we're removing
     * @return the list of SecretKeys that were deleted, otherwise returns null
     */
    List<SecretKey> deleteSecretKeysForUUID(final UUID id);

    /**
     * Removes the current user's PrivateKey.
     *
     * @return the PrivateKey that was removed, otherwise returns null
     */
    PrivateKey deletePrivateKey();

    /**
     * Removes the Message with the associated unique id.
     *
     * @param id the id of the message to remove
     * @return the Message that was removed, otherwise returns null
     */
    Message deleteMessage(final UUID id);

    ////////////////
    // list* methods
    ////////////////

    /**
     * Lists all conversations this device has available.
     *
     * @return a list of Conversations available
     */
    List<Conversation> listAllConversations();

    /**
     * Lists all known dawgIdentifiers.
     *
     * @return a list of DawgIdentifiers this device knows about.
     */
    List<DawgIdentifier> getAllDawgIdentifiers();
}