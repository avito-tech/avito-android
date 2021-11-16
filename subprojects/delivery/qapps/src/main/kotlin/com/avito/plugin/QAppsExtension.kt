package com.avito.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public open class QAppsExtension @Inject constructor(objects: ObjectFactory) {

    public val serviceUrl: Property<String> = objects.property()

    /**
     * Specifies the comment message, visible in QApps interface
     * It could be additional meta information, like build number
     */
    public val comment: Property<String> = objects.property()

    /**
     * Branch name without remote specified
     */
    public val branchName: Property<String> = objects.property()
}
