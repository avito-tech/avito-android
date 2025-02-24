package com.avito.i18n.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property

public abstract class TranslationExtension(
    objectFactory: ObjectFactory
) {
    /**
     * Languages for which translation is carried out.
     * Accepted formats: en, en-US.
     */
    public val locales: SetProperty<String> = objectFactory.setProperty(String::class.java).convention(emptySet())

    /**
     * Default locale. Accepted formats: en, en-US.
     */
    public val defaultLocale: Property<String> = objectFactory.property<String>().convention("")

    /**
     * The name of the component for registering the translation file.
     */
    public abstract val componentName: Property<String>

    /**
     * URL to the translation service.
     */
    public abstract val serviceUrl: Property<String>
}
