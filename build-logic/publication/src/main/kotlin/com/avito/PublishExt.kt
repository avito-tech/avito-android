package com.avito

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.plugins.signing.SigningExtension

internal val Project.publishing: org.gradle.api.publish.PublishingExtension
    get() = extensions.getByName("publishing") as org.gradle.api.publish.PublishingExtension

internal fun Project.signing(configure: Action<SigningExtension>): Unit =
    extensions.configure("signing", configure)
