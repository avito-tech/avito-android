package com.avito.android.lint

import com.avito.slack.model.SlackChannel
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public open class LintReportExtension @Inject constructor(objects: ObjectFactory) {

    // todo some global slack settings?
    public val slackToken: Property<String> = objects.property()
    public val slackWorkspace: Property<String> = objects.property()
    public val slackChannelToReportLintBugs: Property<SlackChannel> = objects.property()
}
