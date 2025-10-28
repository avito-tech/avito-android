package com.avito.deeplink_generator

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.kotlin.dsl.mapProperty
import javax.inject.Inject

/**
 * Extension for specifying allowed deeplink schemes in different application variants.
 *
 * Example: mapOf("debug" to setOf("com.scheme1", "com.scheme2"))
 */
public abstract class DeeplinkManifestFilterExtension @Inject constructor(
    objects: ObjectFactory
) {

    public val variantToAllowedSchemes: MapProperty<String, Set<String>> = objects.mapProperty()
}
