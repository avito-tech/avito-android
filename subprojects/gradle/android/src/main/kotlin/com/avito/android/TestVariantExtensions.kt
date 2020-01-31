@file:Suppress("UnstableApiUsage")

package com.avito.android

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.tasks.PackageAndroidArtifact
import org.gradle.api.tasks.TaskProvider

fun TestVariant.withArtifacts(
    block: (
        testVariantArtifact: TaskProvider<PackageAndroidArtifact>,
        testedVariantArtifact: TaskProvider<PackageAndroidArtifact>
    ) -> Unit
) {
    block(
        packageApplicationProvider,
        (testedVariant as ApplicationVariant).packageApplicationProvider
    )
}
