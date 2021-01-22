package com.avito.android

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.mapProperty
import javax.inject.Inject

typealias Configuration = String
typealias Dependency = String

open class RobolectricPrefetchExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * Specify robolectric dependencies to be prefetched before robolectric tests execution
     *
     * example: mapOf(
     *   "robolectric8", "org.robolectric:android-all:8.0.0_r4-robolectric-r1",
     *   "robolectric10", "org.robolectric:android-all:10-robolectric-5803371"
     * )
     */
    val prefetchDependencies = objects.mapProperty<Configuration, Dependency>()
}
