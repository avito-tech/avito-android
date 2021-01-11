package com.avito.kotlin.dsl

import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionAware

/**
 * gets group of modules (under single root directory, e.g. "avito-app" for avito feature modules),
 * stored previously in evaluation phase (in settings.gradle)
 */
@Suppress("UNCHECKED_CAST")
fun modulesGroup(gradle: Gradle, name: String): Set<String> =
    (gradle as ExtensionAware).extensions.extraProperties[name] as Set<String>
