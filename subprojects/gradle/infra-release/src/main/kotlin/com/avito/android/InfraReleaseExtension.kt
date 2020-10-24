package com.avito.android

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class InfraReleaseExtension @Inject constructor(objects: ObjectFactory) {

    val releaseTag = objects.property<String>()

    val previousReleaseTag = objects.property<String>()
}
