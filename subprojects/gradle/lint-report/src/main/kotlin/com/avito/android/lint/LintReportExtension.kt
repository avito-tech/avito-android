package com.avito.android.lint

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class LintReportExtension @Inject constructor(objects: ObjectFactory) {

    val slackToken = objects.property<String>()

    val slackWorkspace = objects.property<String>()

}
