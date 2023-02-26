package com.scuttlemutt.app.backendimplementations.iomanager;

import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.SecretKey;

import backend.iomanager.IOManager;
import backend.iomanager.IOManagerException;
import backend.iomanager.IOManagerHelper;
import types.DawgIdentifier;
import types.packet.KeyExchangePacket;
import types.packet.Packet;

public class EndpointIOManager implements IOManager {

    private final ConnectionsClient connectionsClient;

    // Mapping of endpointId to endpointName
    private Map<String, String> idToName;

    // Mapping of endpointId to received Packets
    private Map<String, BlockingQueue<Packet>> packetIngestionQueues;


    // Set of trusted endpointIds we trust
    private Set<String> trustedConnections;

    // Mapping of senderName to senderId
    private Map<String, String> currentConnections;

    // Mapping of senderName to UUID
    private Map<String, UUID> seenConnections;

    public EndpointIOManager(ConnectionsClient connectionsClient) {
        this.connectionsClient = connectionsClient;
        this.idToName = new HashMap<>();
        this.packetIngestionQueues = new HashMap<>();
        this.currentConnections = new HashMap<>();
        this.trustedConnections = new HashSet<>();
        this.seenConnections = new HashMap();
    }


    @Override
    public void send(String receiverId, Packet packet) throws IOManagerException {
        if (!currentConnections.keySet().contains(receiverId)) {
            throw new IOManagerException("No available connection to '" + receiverId + "'");
        }
        connectionsClient.sendPayload(currentConnections.get(receiverId), Payload.fromBytes(packet.toNetworkBytes()));
    }

    @Override
    public <T extends Packet> T meshReceive(Class<T> desiredPacketClass) {
        while(true){
            List<BlockingQueue<Packet>> inputs = new ArrayList<>(this.packetIngestionQueues.values());
            synchronized (this.packetIngestionQueues) {
                for(BlockingQueue<Packet> input: this.packetIngestionQueues.values()) {
                    if(!input.isEmpty()){
                                final Optional foundPacketOptional
                                        = IOManagerHelper.getPacketTypeFromBlockingQueue(input, desiredPacketClass);

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

    public KeyExchangePacket isKeyExchangePacket(Packet packet){
        if (packet.getClass() == KeyExchangePacket.class){
            return (KeyExchangePacket) packet;
        }
        return null;
    }

    @Override
    public Set<String> availableConnections() throws IOManagerException {
        return currentConnections.keySet();
    }

    /**
     * Assumes that the endpoint is trusted and adds mapping of sender name to sender id for sending
     * messages
     * @param connectionId String representing sender id (endpoint id) of sender
     * @param connectionName String representing sender name (endpoint name) of sender
     */
    public void addAvailableConnection(String connectionId, String connectionName){
        this.currentConnections.put(connectionName, connectionId);
        // if there is no packet-receiving queue on-hand for any of the connections we're storing,
        // create a new one for that connection.
            if (!this.packetIngestionQueues.containsKey(connectionName)) {
                this.packetIngestionQueues.put(connectionName, new LinkedBlockingQueue<Packet>());
            }

    }


    /**
     * Removes specificed connection from list of available connections
     * @param connectionName Name of connection endpoint
     * @throws IOManagerException if connection not currently connected
     */
    public void removeAvailableConnection(String connectionName) throws IOManagerException {
        if(currentConnections.containsKey(connectionName)) {
            this.currentConnections.remove(connectionName);
        }else {
            throw new IOManagerException("Connection to be removed not found in current connections");
        }

    }

    public void removeAllAvailableConnections(){
        this.currentConnections = new HashMap<>();
    }

    /**
     * Add received message to queues
     * @param connectionName contact name of message sender
     * @param packet Packet from sender
     */
    public void addReceivedMessage(String connectionName, Packet packet){
        synchronized(packetIngestionQueues) {
            if (!packetIngestionQueues.keySet().contains(connectionName)) {
                packetIngestionQueues.put(connectionName, new LinkedBlockingQueue<>());
            }
            BlockingQueue queue = packetIngestionQueues.get(connectionName);
            queue.add(packet);
        }
    }

    /**
     * Add a connection to list of seen connections
     * @param dawgIdentifier dawgIdentifier of user we connected to
     */
    public void addConnection(DawgIdentifier dawgIdentifier){
        seenConnections.put(dawgIdentifier.getUsername(), dawgIdentifier.getUUID());
    }


}
