package com.avito.deeplink_generator

import com.avito.deeplink_generator.internal.parser.DeeplinkParser
import com.avito.deeplink_generator.model.Deeplink
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public abstract class DeeplinkGeneratorExtension @Inject constructor(
    objects: ObjectFactory
) {

    public abstract val publicDeeplinks: SetProperty<Deeplink>

    public abstract val defaultScheme: Property<String>
    public abstract val activityIntentFilterClass: Property<String>
    public val validationCodeFixHint: Property<String> = objects.property<String>()
        .convention("")

    public fun publicDeeplinks(vararg links: String) {
        publicDeeplinks.set(
            links.map { DeeplinkParser.parse(it, defaultScheme.get()) }.toSet()
        )
    }
}
