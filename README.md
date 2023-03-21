# Welcome to the Scuttlemutt repo!

## What is this?

Scuttlemutt is a decentralized, fairly secure, mesh-network-based messaging app for Android.

This repo contains two modules:
- `app`:  This module contains our Android app and Android-specific objects.
- `scuttlemutt-backend`:  This module contains our platform-agnostic network logic + component interfaces.  It also contains a setup for testing this logic on your local machine.

The `app` frontend utilizes the `Scuttlemutt` objects from `scuttlemutt-backend` to work with the backend.

This repo should only be opened in Android Studio.  The Gradle files for the project are written in Android Gradle, so they won't work in any other IDE.

## Contributor information

This is a project inspired by the [Scuttlebutt](https://scuttlebutt.nz/) project. Since Scuttlemutt was developed over a 10-week period as a class project, certain elements were sourced from external sources online.

These sources include:
- [NearbyConnectionsWalkieTalkie Android demo](https://github.com/android/connectivity-samples/tree/main/NearbyConnectionsWalkieTalkie):  Our NearbyConnections API-based Android networking code has elements taken from this demo code provided by Google.
- [Jetchat](https://github.com/android/compose-samples/tree/main/Jetchat):  Our Android UI is a modified version of this messaging app's UI.

Everything else in the project was developed from scratch by the Scuttlemutt group members:
- Dominick Ta
- Nick Durand
- John Taggart
- Justin Shaw
- Amanda Ong

## Installing and running our project.

### Running the app + unit tests.

As mentioned earlier, the Gradle files for this project are configured for Android Studio.  To do anything with our project, you'll need to use Android Studio (Electric Eel or newer).

Within Android Studio, you can run the following Gradle targets:
- Deploy the app to a real device using the `app` target.
  - NOTE:  The app does not work in the Android simulator.  The simulator does not support the NearbyConnections API, so it crashes on launch.
  - NOTE2:  The Android SDK should have a version >= 32.
- Run the unit tests for the classes which utilize the Android APIs via right-clicking the `app` directory and clicking the `Run 'All Tests'` option from the dropdown.
- Run the unit tests for our backend logic via right-clicking the `scuttlemutt-backend` directory and clicking the `Run 'Tests in 'scuttlemutt-backend''` option from the dropdown.

After deploying the app onto two devices, a series of Toast messages should appear near the bottom of the screen. When the message 'Ready to talk' appears, the two devices should be connected with exchanged keys, and messages can be sent. This can be verified by checking that the connected endpoint appears as an option in the chat menu and appears under 'Trusted Connections'.

> **_NOTE:_**  To access the trusted connections page, click the chat menu option on the top left. The trusted connections page can be accessed by clicking the network button on the top right of the menu page. 
### Troubleshooting the app

- Toast 'Permission required: BLUETOOTH_COARSE_LOCATION' appears
  - 1) Restart app
    - Try restarting app by switching to another application (or the home screen) besides the scuttlemutt app and closing app completely
    - Reopen app and permission prompt should Reopen
    - Click 'Allow'
  - 2) Check phone's bluetooth
    - Close app and ensure phone can connect to other bluetooth devices
    - Reopen app

- Message 'Endpoint [endpoint id] discovered' and 'Connection to endpoint [endpoint id] failed' keep appearing
  - Try restarting both apps by completely closing and reopening apps
  - If message continues, try redownloading app onto phones

- 'Ready to talk' only appears on one screen or conversation for contact only appears on one phone
  - Wait 20 seconds
  - Try restarting app which does not contain conversation
  - Uninstall and reinstall app on phones to restart database and reinstate key exchange

- Message not appearing on phone even though connection is initiated and appears in contacts and under trusted connections
  - Wait 20 seconds
  - Close both apps and reopen, then try resending message

### Running the simulation CLI.
The `scuttlemutt-backend` package contains the NetworkSimulation CLI, which can be used to test our mesh-networking logic locally within a simulation.  

Android Studio's console doesn't play nicely with Java Scanner objects (which our CLI depends upon), so this code has to be run in IntelliJ instead (v2022.3.3 or newer).

Due to some Android Gradle versioning issues in IntelliJ, you have to do the following:
- Go to `gradle/libs.versions.toml` and set `androidGradlePlugin = "7.4.0-beta02"`.
- Go to `scuttlemutt-backend/src/backend/simulation/NetworkSimulationCLI.java`.
- Perform a Gradle sync on the entire project (you can do this by clicking the elephant-looking icon in the top-right corner).
- At the top of the class definition, click the little green "play" button on the left column.  At this point, you'll likely be prompted to enter a run configuration.  Enter the following:
  - You should be able to select any modern JDK on your machine to test out the code.  We used OpenJDK 19.
  - For the classpath, select `scuttlemutt.scuttlemutt-backend.main`.
  - Enter `backend.simulation.NetworkSimulationCLI` for the fully-qualified class name.
  - Click "Apply" and "Done".
  - The CLI should then run automatically in the IntelliJ console.  If it doesn't, try hitting the "run" button again.
- When you're done playing with the CLI, reset `androidGradlePlugin = "7.4.0"` in `gradle/libs.versions.toml`.  Otherwise, Android app compilation will be broken.

If you encounter any issues during the above process involving varying Gradle or JDK build versions, clean the project by going to `Build > Clean Project` in the IDE dropdown menu.

### Note for Scuttlemutt devs..  

When implementing Android-specific objects which implement interfaces defined in `scuttlemutt-backend` (ex:  IOManager, StorageManager), implement them in `app` and feed them as parameters to `scuttlemutt-backend` objects when initializing them.  Subsequently, write the any unit tests relevant to these objects in the `androidTest` folder within `app`.  This is necessary for the following reasons:
- `app` depends upon the backend for Scuttlemutt objects.  If _any_ content written in the backend sets an explicit (read:  non-interface/extends-based) class dependency upon the frontend, a dependency loop will occur between the backend and app, breaking compilation.
- `scuttlemutt-backend` is built to be a cross-platform backend, so it is not configured as an Android project.  As a result, objects there cannot be easily built to use the Android-specific objects necessary to use Android libraries (ex:  Activities, Contexts, etc.).  This prohibits any Android-specific objects there from functioning properly. 
  - Similarly, the backend testing libraries are not (and cannot be) configured to work with Android APIs.  This means that Android-specific elements required for API initialization (ex:  `Context` objects) cannot be used in tests there.  Also, any Android-API-involved unit testing requires an Android-specific version of JUnit _and_ the Android simulator, both of which are not included in our cross-platform backend.
