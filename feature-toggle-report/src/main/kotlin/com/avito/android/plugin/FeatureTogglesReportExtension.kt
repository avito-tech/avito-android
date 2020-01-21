package com.avito.android.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class FeatureTogglesReportExtension(objects: ObjectFactory) {

    val slackHook = objects.property<String>()

    val developersToTeam = objects.property<Map<String, String>>()
}
