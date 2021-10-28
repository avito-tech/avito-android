package com.avito.instrumentation.internal

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.LibraryVariant
import com.avito.instrumentation.InstrumentationTestsTask

internal class LibraryVariantConfigurator(variant: LibraryVariant) :
    AndroidVariantConfigurator<LibraryVariant>(variant) {

    override fun configure(task: InstrumentationTestsTask) {
        val androidTestVariant = variant.androidTest

        requireNotNull(androidTestVariant) {
            "Variant '$variant' is used to run instrumentation tests, " +
                "but androidTest configuration is disabled.\n" +
                "Please enable it in config to run tests.\n" +
                "See: https://developer.android.com/reference/tools/gradle-api/7.0" +
                "/com/android/build/api/variant/ApplicationVariant" +
                "#androidTest:com.android.build.api.component.AndroidTest"
        }

        task.testApplication.set(androidTestVariant.artifacts.get(SingleArtifact.APK))
        task.testProguardMapping.set(androidTestVariant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE))
        task.testApplicationPackageName.set(androidTestVariant.applicationId)

        task.instrumentationRunner.set(androidTestVariant.instrumentationRunner.get())
    }
}
