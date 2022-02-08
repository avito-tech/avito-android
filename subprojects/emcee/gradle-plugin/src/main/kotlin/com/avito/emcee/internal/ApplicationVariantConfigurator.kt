package com.avito.emcee.internal

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationVariant
import com.avito.emcee.EmceeTestTask

internal class ApplicationVariantConfigurator(variant: ApplicationVariant) :
    AndroidVariantConfigurator<ApplicationVariant>(variant) {

    override fun configure(task: EmceeTestTask) {
        val androidTestVariant = variant.androidTest

        requireNotNull(androidTestVariant) {
            "Variant '$variant' is used to run instrumentation tests, " +
                "but androidTest configuration is disabled.\n" +
                "Please enable it in config to run tests.\n" +
                "See: https://developer.android.com/reference/tools/gradle-api/7.0" +
                "/com/android/build/api/variant/ApplicationVariant" +
                "#androidTest:com.android.build.api.component.AndroidTest"
        }

        task.apk.set(variant.artifacts.get(SingleArtifact.APK))
        task.testApk.set(androidTestVariant.artifacts.get(SingleArtifact.APK))

        // todo need proguard mapping?
        // todo need application package name?
        // todo need instrumentationRunner name?
    }
}
