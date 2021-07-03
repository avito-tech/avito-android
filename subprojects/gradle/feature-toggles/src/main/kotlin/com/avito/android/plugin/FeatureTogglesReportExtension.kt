package com.avito.android.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

public open class FeatureTogglesReportExtension(objects: ObjectFactory) {

    public val slackHook: Property<String> = objects.property()

    public val developersToTeam: MapProperty<String, String> = objects.mapProperty()
}
