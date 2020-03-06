---
title: Known Issues
type: docs
---


# Known Issues

## New Gradle Sync is not supported due to containing Kotlin modules

Disable **Settings > Experimental > Only sync the active variant**

https://stackoverflow.com/questions/54201216/android-studio-3-3-new-gradle-sync-is-not-supported-due-to-containing-kotlin-mo/

## Tests run from Android Studio are failed in runtime with `java.lang.NoClassDefFoundError`

The problem occurs only when running a test from Android Studio because of wrongly generated Run Configuration.
Wherein test run from command line via `./gradlew test` ends up normally.  
Mostly this reproduced in tests that use Kotlin extensions from Test Fixtures from other Gradle modules.

Workaround:
1. Use IntelliJ IDEA;
2. Open **Settings > Build, Execution, Deployment > Build Tools > Gradle **;
3. In drop-down menu **Run test using** select **Gradle** instead of **IntelliJ IDEA**.
