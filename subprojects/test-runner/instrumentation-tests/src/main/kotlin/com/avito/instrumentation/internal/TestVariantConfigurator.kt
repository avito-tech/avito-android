package com.avito.instrumentation.internal

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.TestVariant
import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension

/**
 * Variant supporting 'com.android.test' plugin.
 */
internal class TestVariantConfigurator(
    variant: TestVariant,
    extension: InstrumentationTestsPluginExtension,
) :
    AndroidVariantConfigurator<TestVariant>(variant) {
    private val macrobenchmarkExtension = extension.macrobenchmark

    override fun configure(task: InstrumentationTestsTask) {
        task.testApplication.set(variant.artifacts.get(SingleArtifact.APK))
        task.testApplicationPackageName.set(variant.applicationId)
        task.instrumentationRunner.set(variant.instrumentationRunner.get())

        val applicationBuildDir = macrobenchmarkExtension.applicationBuildDir
        require(applicationBuildDir.isPresent) {
            "Tested application apk file location must be set explicitly via " +
                "[InstrumentationTestsPluginExtension.macrobenchmark]"
        }
        task.application.set(applicationBuildDir.get())

        require(macrobenchmarkExtension.applicationPackageName.isPresent) {
            "Tested application package name must be set explicitly via " +
                "[InstrumentationTestsPluginExtension.macrobenchmark]"
        }
        val applicationPackageName = macrobenchmarkExtension.applicationPackageName.get()
        require(applicationPackageName.isNotBlank()) {
            "Tested application package name must not be empty or blank"
        }
        task.applicationPackageName.set(applicationPackageName)
    }
}
