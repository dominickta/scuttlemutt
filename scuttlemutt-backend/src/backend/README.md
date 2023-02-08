This directory contains the classes associated with backend/network-facing functionality.
- IOManager:  Handles interfacing our mesh network protocols w/ the hardware-level APIs.
- MeshDaemon:  Organizes + handles mesh networking.  This includes packaging barks in network format + sending/receiving/rebroadcasting existing messages.
- Simulation:  Contains classes used to simulate the network.  Useful for testing out code w/o Android devices.