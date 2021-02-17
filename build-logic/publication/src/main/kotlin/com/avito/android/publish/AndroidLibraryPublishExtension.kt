package com.avito.android.publish

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class AndroidLibraryPublishExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * android library variant to be published
     *
     * default: release
     */
    @Suppress("UnstableApiUsage")
    val variant: Property<String> = objects.property<String>().convention("release")
}
