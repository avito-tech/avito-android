package com.avito.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class QAppsExtension @Inject constructor(objects: ObjectFactory) {

    val serviceUrl = objects.property<String>()

    /**
     * Specifies the comment message, visible in QApps interface
     * It could be additional meta information, like build number
     */
    val comment = objects.property<String>()

    /**
     * Branch name without remote specified
     */
    val branchName = objects.property<String>()
}
