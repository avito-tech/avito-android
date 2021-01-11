package com.avito.android.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

open class FeatureTogglesReportExtension(objects: ObjectFactory) {

    val slackHook = objects.property<String>()

    val developersToTeam = objects.mapProperty<String, String>()
}
