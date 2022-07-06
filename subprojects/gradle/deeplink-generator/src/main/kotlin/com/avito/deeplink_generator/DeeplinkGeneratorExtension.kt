package com.avito.deeplink_generator

import com.avito.deeplink_generator.internal.parser.DeeplinkParser
import com.avito.deeplink_generator.model.Deeplink
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public abstract class DeeplinkGeneratorExtension {

    public abstract val publicDeeplinks: SetProperty<Deeplink>

    public abstract val defaultScheme: Property<String>
    public abstract val activityIntentFilterClass: Property<String>
    public abstract val publicDeeplinksFromCode: RegularFileProperty
    public abstract val validationCodeFixHint: Property<String>

    public fun publicDeeplinks(vararg links: String) {
        publicDeeplinks.set(
            links.map { DeeplinkParser.parse(it, defaultScheme.get()) }.toSet()
        )
    }
}
