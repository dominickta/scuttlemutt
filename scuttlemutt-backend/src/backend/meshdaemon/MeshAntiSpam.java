package backend.meshdaemon;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import types.Bark;
import types.packet.BarkPacket;

/**
 *  Detects spam and advises dropping packets. Not all drops are related
 *  to spamming, but spam violations will result in a sender ban.
 *
 *  Violations (drop and ban):
 *  - Sender is spamming the same bark packet or bark. A user should
 *    never send more than once.
 *  - Sender is spamming many different bark packets faster than a pre-
 *    determined max rate.
 *
 *  Non-violations (drop only)
 *  - Receiving the same bark packet or bark from different senders. This
 *    is expected to happen with broadcasts, but should still be dropped.
 */
public class MeshAntiSpam {
    // Spam detection config
    private final MeshAntiSpamConfig config;

    // External seen messages set
    private Set<Bark> seenMessages;

    // For resend spamming
    private Map<BarkPacket, Set<String>> seenBarkPackets;
    private Map<Bark, Set<String>> seenBarks;

    // For rate spamming
    private Map<String, Deque<Double>> barkPacketWindows;
    private Map<String, Deque<Double>> barkWindows;

    // Set of users that have violated spam rules
    private Set<String> bannedUsers;

    /**
     * Builds a spam detector with non-persistent storage.
     * Uses default configuration.
     *
     * @param seenMessages a thread-safe set of messages that may
     *                     be updated from the outside.
     */
    public MeshAntiSpam(Set<Bark> seenMessages) {
        this(seenMessages, new MeshAntiSpamConfig());
    }

    /**
     * Builds a spam detector with non-persistent storage.
     * Uses a custom configuration.
     *
     * @param seenMessages a thread-safe set of messages that may
     *                     be updated from the outside.
     * @param config config object for tuning the spam detection.
     */
    public MeshAntiSpam(Set<Bark> seenMessages, final MeshAntiSpamConfig config) {
        this.config = config;
        this.seenMessages = seenMessages;

        this.seenBarkPackets = new HashMap<>();
        this.seenBarks = new HashMap<>();

        this.barkPacketWindows = new HashMap<>();
        this.barkWindows = new HashMap<>();

        this.bannedUsers = new HashSet<>();
    }

    /**
     * Determines if a particular bark packet should be dropped.
     * There are two reasons a bark should be dropped:
     *  - The bark packet has been seen before.
     *  - The sender is banned.
     *
     * Monitors violating behavior to determine if the sender
     * should be added to the ban list.
     *
     * @param sender the name of the immediate sender.
     * @param barkPacket a bark packet that was sent from the sender.
     * @return true if the bark packet should be dropped.
     */
    public boolean shouldDropBarkPacket(String sender, BarkPacket barkPacket) {
        if (this.bannedUsers.contains((sender))) {
            return true;
        }

        // Check if this sender is resending too many times.
        if (this.seenBarkPackets.containsKey(barkPacket)) {
            if (this.seenBarkPackets.get(barkPacket).contains(sender)) {
                this.bannedUsers.add(sender);
                return true;
            }
        } else {
            this.seenBarkPackets.put(barkPacket, new HashSet<>());
        }
        this.seenBarkPackets.get(barkPacket).add(sender);

        // Check if this sender is sending too fast.
        if (this.windowCheck(sender, this.barkPacketWindows) > this.config.maxBarkPacketRate) {
            this.bannedUsers.add(sender);
            return true;
        }

        return false;
    }

    /**
     * Determines if a particular bark should be dropped.
     * There are two reasons a bark should be dropped:
     *  - The bark has been seen before.
     *  - The sender is banned.
     *
     * Monitors violating behavior to determine if the sender
     * should be added to the ban list.
     *
     * @param sender the name of the immediate sender.
     * @param bark a bark that was sent from the sender.
     * @return true if the bark should be dropped.
     */
    public boolean shouldDropBark(String sender, Bark bark) {
        if (this.bannedUsers.contains((sender))) {
            return true;
        }

        // Check if this sender is resending too many times.
        if (this.seenBarks.containsKey(bark)) {
            if (this.seenBarks.get(bark).contains(sender)) {
                this.bannedUsers.add(sender);
                return true;
            }
        } else {
            this.seenBarks.put(bark, new HashSet<>());
        }
        this.seenBarks.get(bark).add(sender);

        // Check if this sender is sending too fast.
        if (this.windowCheck(sender, this.barkWindows) > this.config.maxBarkRate) {
            this.bannedUsers.add(sender);
            return true;
        }

        // Check if this is a valid rebroadcast.
        if (this.seenMessages.contains(bark)) {
            return true;
        } else {
            this.seenMessages.add(bark);
        }

        return false;
    }

    /**
     * Updates sliding windows of timestamps for rate detection.
     *
     * @param sender sender to be checked.
     * @param windows mapping of all windows.
     * @return the rate of messages per second (0 if first message).
     */
    private double windowCheck(String sender, Map<String, Deque<Double>> windows) {
        double timestamp = System.currentTimeMillis() / 1000.0;
        if (!windows.containsKey(sender)) {
            windows.put(sender, new ArrayDeque<>(this.config.windowSize));
            windows.get(sender).addLast(timestamp);
            return 0;
        } else {
            Deque<Double> window = windows.get(sender);
            if (window.size() >= this.config.windowSize) {
                window.removeFirst();
            }
            window.addLast(timestamp);

            // Return average send rate of the current window
            return (timestamp - window.getLast()) / window.size();
        }
    }

    /**
     * Simple configuration class used by MeshAntiSpam.
     * Uses public fields for simplicity.
     */
    public static class MeshAntiSpamConfig {
        // Max rate of packets per second.
        // Exceeding this value will cause ban.
        // Must be > 0 or all packets will be spam.
        public double maxBarkPacketRate;

        // Max rate of barks per second.
        // Exceeding this value will cause ban.
        // Must be > 0 or all barks will be spam.
        public double maxBarkRate;

        // Max size of "sliding window" for spam detection.
        // Must be > 1, larger values are more forgiving.
        public int windowSize;

        public MeshAntiSpamConfig() {
            maxBarkPacketRate = 10.0;
            maxBarkRate = 100.0;
            windowSize = 10;
        }
    }
}
