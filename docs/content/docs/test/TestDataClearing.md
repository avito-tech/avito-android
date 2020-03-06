---
title: Test Data Clearing
type: docs
---

# Test Data Clearing

Every test runs in clear application state without any database files and SharedPreferences.

## Running locally

When a test is running locally all produced data is cleared on test finish with ADB shell command `pm clear`.
You can control this behaviour of Android Test Orchestrator with `clearPackageData` flag like the following:

```groovy
android {
    defaultConfig {
        testInstrumentationRunner = "com.avito.android.runner.AvitoInstrumentTestRunner"
        testInstrumentationRunnerArguments {
            [
                // ...
                "clearPackageData"            : "true"
            ]
        }
    }
}
```

For more information read [official documentation](https://developer.android.com/training/testing/junit-runner#using-android-test-orchestrator)
for Android Test Orchestrator.

## Running on CI

When a test is running on CI, Test Runner clears all data right before test run using the same `pm clear` command.
Look at `com.avito.runner.service.worker.device.adb.AdbDevice.clearPackage` for more details about implementation.