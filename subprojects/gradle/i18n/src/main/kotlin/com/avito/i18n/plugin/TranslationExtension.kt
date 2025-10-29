package com.avito.i18n.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property

public abstract class TranslationExtension(
    objectFactory: ObjectFactory
) {
    public val locales: SetProperty<String> = objectFactory.setProperty(String::class.java).convention(emptySet())

    public abstract val sourceLocale: Property<String>

    public abstract val componentName: Property<String>

    public abstract val namespace: Property<String>

    public abstract val serviceUrl: Property<String>

    public abstract val translateUrlPath: Property<String>

    public val useTls: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val useNewApi: Property<Boolean> = objectFactory.property<Boolean>().convention(false)
}
