The RoomStorageManager is used to interface with Android's SQL-based Room DB.

Since having the RoomStorageManager used by the backend would introduce circular dependencies
between the app and the backend, it has to be located here.  When creating Scuttlemutt objects, 
the RoomStorageManager should be passed in as the StorageManager for the object.

The subfolders in this directory contain the Entry and Dao objects used by the RoomStorageManager for queries.