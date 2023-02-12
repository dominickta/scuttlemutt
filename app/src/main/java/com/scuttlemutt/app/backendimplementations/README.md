This folder contains objects which implement interfaces defined in the backend (ex:  StorageManager, IOManager).

These implementations _must_ be done here instead of the backend due to circular dependency issues + Android platform-specific dependency issues.  (see README at repo root for more details)

The JUnit tests for these objects should be stored in the corresponding folder(s) within the `app` package's `androidTest` directory.