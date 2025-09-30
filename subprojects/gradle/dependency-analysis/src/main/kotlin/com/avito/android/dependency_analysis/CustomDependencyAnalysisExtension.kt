package com.avito.android.dependency_analysis

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

public abstract class CustomDependencyAnalysisExtension {
    /**
        Maps dependency identifiers to their aliases from version catalogs.

        For instance, if the version catalog "libs" contains line
        ```
        kotlinStdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
        ```
        then this property maps "org.jetbrains.kotlin:kotlin-stdlib" to "libs.kotlinStdlib"
     */
    internal abstract val dependencyMap: MapProperty<String, String>

    /**
     * List of identifiers that should be ignored
     */
    public abstract val checkDependenciesForBreakingAbiExclusions: RegularFileProperty

    /**
     * Failure message to append to the error text. It can contain a link to internal docs.
     */
    public abstract val checkDependenciesForBreakingAbiFailureMessage: Property<String>
}
