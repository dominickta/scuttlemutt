This directory contains the StorageManager-related classes.

Although we could construct a complex set of StorageManager classes uniquely scoped to handling each data type, we have few enough data types that one monolithic class should work.

There is only one StorageManager class at the moment:  the Map-based MapStorageManager.

TODO:  In the future, we should setup a StorageManager solution which uses a proper DB.