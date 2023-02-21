package backend.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import backend.scuttlemutt.Scuttlemutt;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;

/**
 * Class used to interact with a simulation of the Scuttlemutt over CLI.
 */
public class NetworkSimulationCLI {
    private static final String NO_DEVICE_ERROR_MSG = "No device is currently selected.  Please select a device on the network!";
    private static final String LABEL_PREFIX = "device_";
    private static final String HELP_PRINTOUT = "Valid commands:\n"
            + "--help/-h:  prints this help doc\n"
            + "send-message/sm:  sends a message.  args:  -m: message\t-d: device id of destination\n"
            + "print-conversations/pc:  prints metadata about the conversations for the currently selected device.\n"
            + "select-device/sd:  selects the network device to interact with.  args: -d: the deviceID of the device we wish to interact with.\n"
            + "print-current-device/pd:  prints the currently-selected device we're interacting with.\n"
            + "print-messages/pm:  prints the messages in the specified conversation.  args: -d: the other party in the conversation\n"
            + "connect-devices/cd:  creates a connection between the specified devices.  args:  -d1:  the deviceID of one of the devices.  -d2:  the deviceID of the other device.\n"
            + "disconnect-devices/dd:  removes a connection between the specified devices.  args:  -d1:  the deviceID of one of the devices.  -d2:  the deviceID of the other device.\n"
            + "exit:  exits the CLI.";

    private static NetworkSimulation simulation = null; // the simulation of the network.
    private static int numDevices;
    private static Scuttlemutt currentDevice = null; // the device currently selected by the CLI. This device is used to
                                                     // interact with the network.
    private static Integer currentDeviceId = null; // the ID corresponding with current device.

    /**
     * Main method for CLI.
     * 
     * @param args args[0] = number of devices on network.
     */
    public static void main(String[] args) {
        final Scanner s = new Scanner(System.in);

        // get the desired number of devices on the network.
        System.out.println(
                "Welcome to the Scuttlemutt CLI!  Please provide the number of devices you wish to initialize on the network.");

        // initialize with devices labeled device0..device<N>.
        numDevices = s.nextInt();
        final List<String> deviceLabels = new ArrayList<String>();
        for (int i = 0; i < numDevices; i++) {
            deviceLabels.add(LABEL_PREFIX + i);
        }
        simulation = new NetworkSimulation(deviceLabels);
        simulation.connectAll();

        System.out.println(
                "Please enter your desired commands.  (NOTE:  We recommend setting a device to interact with first.)");

        /*
         * REPL loop used to interact w/ network simulation.
         */
        while (true) {
            // get the input from the scanner.
            final String input = s.nextLine();

            // tokenize the input.
            final String[] tokens = input.split(" ");

            // analyze the input.
            if (tokens[0].equals("help") || tokens[0].equals("h")) {
                printOpsToolsOptions();

            } else if (tokens[0].equals("send-message") || tokens[0].equals("sm")) {
                final String msg, dstDeviceId;
                if (tokens[1].equals("-m") && tokens[3].equals("-d")) {
                    msg = tokens[2];
                    dstDeviceId = tokens[4];
                } else if (tokens[1].equals("-d") && tokens[3].equals("-m")) {
                    msg = tokens[4];
                    dstDeviceId = tokens[2];
                } else {
                    System.out.println(
                            "Please specify the message being sent using the `-m` flag and specify the destination device id using the `-d` flag when calling send-message!");
                    continue;
                }

                // lookup the destination device UUID.
                final DawgIdentifier dstDeviceDawgId = lookupDawgIdentifier(dstDeviceId);

                // send the message.
                sendMessage(msg, dstDeviceDawgId);

                System.out.println("Successfully sent the message to " + dstDeviceId + "!");

            } else if (tokens[0].equals("connect-devices") || tokens[0].equals("cd")) {
                final String deviceId1, deviceId2;
                if (tokens[1].equals("-d1") && tokens[3].equals("-d2")) {
                    deviceId1 = LABEL_PREFIX + tokens[2];
                    deviceId2 = LABEL_PREFIX + tokens[4];
                } else if (tokens[1].equals("-d2") && tokens[3].equals("-d1")) {
                    deviceId1 = LABEL_PREFIX + tokens[4];
                    deviceId2 = LABEL_PREFIX + tokens[2];
                } else {
                    System.out.println(
                            "Please specify the two devices in the connection being created using the `-d1` and `-d2` flags when calling connect-devices!  Order does not matter.");
                    continue;
                }

                // create the connections.
                connectDevices(deviceId1, deviceId2);

                System.out.println("Successfully connected  " + deviceId1 + " to " + deviceId2 + "!");

            } else if (tokens[0].equals("disconnect-devices") || tokens[0].equals("dd")) {
                final String deviceId1, deviceId2;
                if (tokens[1].equals("-d1") && tokens[3].equals("-d2")) {
                    deviceId1 = LABEL_PREFIX + tokens[2];
                    deviceId2 = LABEL_PREFIX + tokens[4];
                } else if (tokens[1].equals("-d2") && tokens[3].equals("-d1")) {
                    deviceId1 = LABEL_PREFIX + tokens[4];
                    deviceId2 = LABEL_PREFIX + tokens[2];
                } else {
                    System.out.println(
                            "Please specify the two devices in the connection being removed using the `-d1` and `-d2` flags when calling disconnect-devices!  Order does not matter.");
                    continue;
                }

                // create the connections.
                disconnectDevices(deviceId1, deviceId2);

                System.out.println("Successfully disconnected  " + deviceId1 + " to " + deviceId2 + "!");

            } else if (tokens[0].equals("print-conversations") || tokens[0].equals("pc")) {
                printConversations();

            } else if (tokens[0].equals("select-device") || tokens[0].equals("sd")) {
                // validate input.
                if (tokens.length < 3 || !tokens[1].equals("-d")) {
                    System.out.println(
                            "Please specify the device id number using the `-d` flag when calling select-device!");
                    continue;
                }

                // call selectDevice.
                selectDevice(Integer.parseInt(tokens[2]));

                System.out.println("Successfully selected device " + tokens[2] + "!");

            } else if (tokens[0].equals("print-current-device") || tokens[0].equals("pd")) {
                printCurrentDeviceNumber();

            } else if (tokens[0].equals("print-messages") || tokens[0].equals("pm")) {
                // validate input.
                if (tokens.length < 3 || !tokens[1].equals("-d")) {
                    System.out.println(
                            "Please specify the device id of the other party in the conversation using the `-d` flag when calling print-messages!");
                    continue;
                }

                // get the other device's ID.
                final DawgIdentifier dstDawgId = lookupDawgIdentifier(tokens[2]);

                // get the associated Conversation.
                if (currentDevice == null) {
                    System.out.println(NO_DEVICE_ERROR_MSG);
                    continue;
                }
                final Conversation c = currentDevice.getConversation(dstDawgId);

                // call printMessages using the obtained Conversation.
                printMessages(c);

            } else if (tokens[0].equals("exit")) {
                simulation.shutdown();
                s.close();
                System.exit(0);
            } else {
                System.out.println(HELP_PRINTOUT);
            }
        }
    }

    private static void printOpsToolsOptions() {
        System.out.println(HELP_PRINTOUT);
    }

    private static void sendMessage(final String msg, final DawgIdentifier dstDawgId) {
        if (currentDevice == null) { // check if any device is selected.
            System.out.println(NO_DEVICE_ERROR_MSG);
        } else {
            // prepend the message with the current device to ID who sent what.
            final String prependedMsg = currentDeviceId + ":  " + msg;

            currentDevice.sendMessage(prependedMsg, dstDawgId);
        }
    }

    private static void selectDevice(final int id) {
        // obtain the QueueIOManager for the device.
        // (the NetworkSimulation object handles the validation.)
        currentDevice = simulation.getScuttlemutt(LABEL_PREFIX + id);
        currentDeviceId = id;
    }

    private static void printCurrentDeviceNumber() {
        if (currentDevice == null) { // check if any device is selected.
            System.out.println(NO_DEVICE_ERROR_MSG);
        } else {
            System.out.println("The currently selected device is:  " + currentDeviceId);
        }
    }

    private static void printConversations() {
        // lookup all ongoing Conversations from database for the user.
        final List<Conversation> conversations = currentDevice.listAllConversations();

        // print out the conversations + stats about each.
        for (final Conversation c : conversations) {
            // TODO:  Replace this with code which actually looks up full msgs instead of Barks.
            //   (if we don't do this though, the demo should still work given a short enough msg)
            
            // lookup the number of msgs in the Conversation.
            final int numMsgs = c.getMessageUUIDList().size();

            // print out info about the Conversation.
            System.out.println("device:  " + c.getOtherPerson().getUsername() + "\tmsg count:  " + numMsgs);
        }

        System.out.println("done");
    }

    private static void printMessages(final Conversation conversation) {
        // obtain the msgs.
        final List<Message> msgs = currentDevice.getMessagesForConversation(conversation);

        // print the msgs out.
        for (final Message m : msgs) {
            System.out.println(m.getPlaintextMessage());
        }
    }

    private static void connectDevices(final String deviceId1, final String deviceId2) {
        simulation.disconnectDevices(deviceId1, deviceId2);
    }

    private static void disconnectDevices(final String deviceId1, final String deviceId2) {
        simulation.disconnectDevices(deviceId1, deviceId2);
    }

    /**
     * Returns the UUID for the specified device.
     *
     * NOTE: If the UUID is nonexistent, NetworkSimulation.getScuttlemutt() will
     * throw an exception.
     *
     * @param deviceId The ID of the device whose UUID is being looked-up.
     * @return The UUID of the specified device.
     */
    private static DawgIdentifier lookupDawgIdentifier(final String deviceId) {
        return simulation.getScuttlemutt(LABEL_PREFIX + deviceId)
                .getDawgIdentifier();
    }
}