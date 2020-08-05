./gradlew samples:test-app-screenshot-test:clearScreenshots
./gradlew samples:test-app-screenshot-test:connectedAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.annotation=com.avito.android.test.annotations.ScreenshotTest \
    -PrecordScreenshotsMode
./gradlew samples:test-app-screenshot-test:recordScreenshots
