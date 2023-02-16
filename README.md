# Scuttlemutt Repo

This repo should only be opened in Android Studio.  The Gradle files for the project are written in Android Gradle, so they won't work in any other IDE.

This repo contains two modules:
- `app`:  This module contains our Android app and Android-specific objects.
- `scuttlemutt-backend`:  This module contains our network logic + component interfaces.

The `app` frontend utilizes the `Scuttlemutt` objects from `scuttlemutt-backend` to work with the backend.

NOTE FOR DEVS:  When implementing Android-specific objects which implement interfaces defined in `scuttlemutt-backend` (ex:  IOManager, StorageManager), implement them in `app` and feed them as parameters to `scuttlemutt-backend` objects when initializing them.  Subsequently, write the any unit tests relevant to these objects in the `androidTest` folder within `app`.  This is necessary for the following reasons:
- `app` depends upon the backend for Scuttlemutt objects.  If _any_ content written in the backend sets an explicit (read:  non-interface/extends-based) class dependency upon the frontend, a dependency loop will occur between the backend and app, breaking compilation.
- `scuttlemutt-backend` is built to be a cross-platform backend, so it is not configured as an Android project.  As a result, objects there cannot be easily built to use the Android-specific objects necessary to use Android libraries (ex:  Activities, Contexts, etc.).  This prohibits any Android-specific objects there from functioning properly. 
  - Similarly, the backend testing libraries are not (and cannot be) configured to work with Android APIs.  This means that Android-specific elements required for API initialization (ex:  `Context` objects) cannot be used in tests there.  Also, any Android-API-involved unit testing requires an Android-specific version of JUnit _and_ the Android simulator, both of which are not included in our cross-platform backend.
