package com.avito.android.publish

import org.gradle.api.provider.Property

abstract class KotlinLibraryPublishExtension {

    /**
     * non blank value will modify artifact id of maven coordinates
     *
     * default: project.name
     */
    abstract val artifactId: Property<String>
}
