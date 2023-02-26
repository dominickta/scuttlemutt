This directory contains the StorageManager-related classes.

Although we could construct a complex set of StorageManager classes uniquely scoped to handling each data type, we have few enough data types that one monolithic class should work.

The map-based storage manager here is used primarily for testing. The production app has a Room-based database which implements StorageManager, see the app folder for more info.