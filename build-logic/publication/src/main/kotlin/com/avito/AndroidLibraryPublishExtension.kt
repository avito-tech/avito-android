package com.avito.android.publish

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class AndroidLibraryPublishExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * android library variant to be published
     *
     * default: release
     */
    val variant: Property<String> = objects.property(String::class.java).convention("release")

    /**
     * non blank value will modify artifact id of maven coordinates
     *
     * default: project.name
     */
    abstract val artifactId: Property<String>
}
