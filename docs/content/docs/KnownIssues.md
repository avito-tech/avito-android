---
title: Known Issues
type: docs
---


# Known Issues

## New Gradle Sync is not supported due to containing Kotlin modules

Disable **Settings > Experimental > Only sync the active variant**

https://stackoverflow.com/questions/54201216/android-studio-3-3-new-gradle-sync-is-not-supported-due-to-containing-kotlin-mo/

## Instrumentation tests run from Android Studio are failed with `java.lang.NoClassDefFoundError`

The problem occurs when running a test from Android Studio because of generated Run Configuration has inappropriate type.
Wherein test run from command line via `./gradlew test` ends up normally.  
Mostly this reproduced in tests that use Kotlin extensions from Test Fixtures from other Gradle modules.

Workaround:
1. Use IntelliJ IDEA;
1. Open **Settings > Build, Execution, Deployment > Build Tools > Gradle **;
1. In drop-down menu **Run test using** select **Gradle** instead of **IntelliJ IDEA**;
1. Then go to "Edit Configurations..." (which is is the drop-down menu to the right of Run button);
1. Remove old Run Configuration with "-" button;
1. Run test again (make sure new configuration is created).
