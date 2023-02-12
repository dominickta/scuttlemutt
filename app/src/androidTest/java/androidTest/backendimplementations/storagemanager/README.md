Contains the tests for the Android implementations of backend interfaces.

The tests _must_ be located here in the `app` package to prevent a circular dependency loop from
forming betweent the frontend and backend.

NOTE:  Unlike our JUnit 5-based tests in our backend, Android's test library is a modified version
of JUnit 4.  This results in some annotation inconsistencies in how tests are written between the two
packages.

NOTE2:  These tests rely upon frameworks + data stored in the Android OS (ex:  Context objects).  To run these tests, your 
Android Studio setup _must_ have a virtual Android device setup + connected.

NOTE3:  In order for any unit test to be run on the Android simulator or a connected device, it _must_ be located in this
directory.  Otherwise, it will not be recognized and will be ignored/run incorrectly and fail/etc.