package com.avito.android.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class RobolectricConventionExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * We add androidx.test.core to use in pair with robolectric
     */
    val androidXTestVersion = objects.property<String>()

    /**
     * :test:robolectric version
     */
    val avitoRobolectricLibVersion = objects.property<String>()

    /**
     * True to access resources in robolectric tests out of the box
     */
    val includeAndroidResources: Property<Boolean> = objects.property<Boolean>().convention(true)

    /**
     * Adding robolectric(and supportive) dependencies to
     */
    val targetConfiguration: Property<String> = objects.property<String>().convention("testImplementation")

    /**
     * add task dependency for RobolectricPrefetchPlugin
     */
    val wirePrefetchPlugin = objects.property<Boolean>()
}
