package com.scuttlemutt.app.backendimplementations.iomanager;

import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import backend.iomanager.IOManager;
import backend.iomanager.IOManagerException;
import backend.iomanager.IOManagerHelper;
import types.packet.Packet;

public class EndpointIOManager implements IOManager {

    private final ConnectionsClient connectionsClient;

    // Mapping of endpointId to endpointName
    private Map<String, String> idToName;

    // Mapping of endpointId to received Packets
    private Map<String, BlockingQueue<Packet>> packetIngestionQueues;

    // Set of currently current connections
    public Set<String> currentConnections;

    // Set of trusted endpointIds we trust
    private Set<String> trustedConnections;

    // Mapping of endpointId to public key
    private Map<String, String> endpointKeys;

    public EndpointIOManager(ConnectionsClient connectionsClient) {
        this.connectionsClient = connectionsClient;
        this.idToName = new HashMap<>();
        this.packetIngestionQueues = new HashMap<>();
        this.currentConnections = new HashSet<>();
        this.trustedConnections = new HashSet<>();
        this.endpointKeys = new HashMap<>();
    }


    @Override
    public void send(String receiverId, Packet packet) throws IOManagerException {
        if (!currentConnections.contains(receiverId)) {
            throw new IOManagerException("No available connection to '" + receiverId + "'");
        }
        connectionsClient.sendPayload(receiverId, Payload.fromBytes(packet.toNetworkBytes()));
    }

    @Override
    public <T extends Packet> T meshReceive(Class<T> desiredPacketClass) {
        while(true){
            if(packetIngestionQueues.values().size() > 0){
                for(String key: packetIngestionQueues.keySet()) {
                    if(packetIngestionQueues.get(key).size() > 0) {
                        BlockingQueue queue = packetIngestionQueues.get(key);
                        final Optional foundPacketOptional
                                = IOManagerHelper.getPacketTypeFromBlockingQueue(queue, desiredPacketClass);

                        // if a Packet of the desired type was found, return it.
                        if (foundPacketOptional.isPresent()) {
                            return (T) foundPacketOptional.get();
                        }
                    }
                }
            }
        }
    }


    @Override
    public <T extends Packet> T singleDeviceReceive(String senderId, Class<T> desiredPacketClass) {
        BlockingQueue queue = packetIngestionQueues.get(senderId);
        while(true) {
            if (queue.size() > 0) {
                final Optional foundPacketOptional
                        = IOManagerHelper.getPacketTypeFromBlockingQueue(queue, desiredPacketClass);

                // if a Packet of the desired type was found, return it.
                if (foundPacketOptional.isPresent()) {
                    return (T) foundPacketOptional.get();
                }
            }
        }
    }

    @Override
    public Set<String> availableConnections() throws IOManagerException {
        return currentConnections;
    }

    /**
     * Assumes that the endpoint is trusted
     */
    public void updateAvailableConnection(Set<String> currentConnections){
        this.currentConnections = currentConnections;

        // if there is no packet-receiving queue on-hand for any of the connections we're storing,
        // create a new one for that connection.
        for (final String conn : currentConnections) {
            if (!this.packetIngestionQueues.containsKey(conn)) {
                this.packetIngestionQueues.put(conn, new LinkedBlockingQueue<Packet>());
            }
        }
    }

    public void addReceivedMessage(String senderId, Packet packet){
        BlockingQueue queue = packetIngestionQueues.get(senderId);
        queue.add(packet);
    }
}
