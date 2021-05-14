package com.avito.android

import org.gradle.api.provider.Property

abstract class ChangedTestsFinderExtension {

    abstract val targetCommit: Property<String>
}
