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
            synchronized (inputs) {
                for(BlockingQueue<Packet> input: inputs) {
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

    public KeyExchangePacket isSecretKey(Packet packet){
        if(packet.getClass() == KeyExchangePacket.class){
            return (KeyExchangePacket) packet;
        }
        return null;
    }

    @Override
    public Set<String> availableConnections() throws IOManagerException {
        return currentConnections.keySet();
    }

    /**
     * Assumes that the endpoint is trusted
     */
    public void addAvailableConnection(String senderId, String senderName){
        this.currentConnections.put(senderName, senderId);
        // if there is no packet-receiving queue on-hand for any of the connections we're storing,
        // create a new one for that connection.
            if (!this.packetIngestionQueues.containsKey(senderName)) {
                this.packetIngestionQueues.put(senderName, new LinkedBlockingQueue<Packet>());
            }

    }


    public void removeAvailableConnection(String senderName){
        this.currentConnections.remove(senderName);

    }

    public void removeAllAvaliableConnections(){
        this.currentConnections = new HashMap<>();
    }

    public void addReceivedMessage(String senderName, Packet packet){
        synchronized(packetIngestionQueues) {
            if (!packetIngestionQueues.keySet().contains(senderName)) {
                packetIngestionQueues.put(senderName, new LinkedBlockingQueue<>());
            }
            BlockingQueue queue = packetIngestionQueues.get(senderName);
            queue.add(packet);
        }
    }

    public boolean seenConnection(String senderId){
        return seenConnections.keySet().contains(senderId);
    }

    public void addConnection(DawgIdentifier dawgIdentifier){
        seenConnections.put(dawgIdentifier.getUsername(), dawgIdentifier.getUUID());
    }


}
