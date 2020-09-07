package com.avito.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class TmsExtension @Inject constructor(objects: ObjectFactory) {

    val reportsHost = objects.property<String>()
}
