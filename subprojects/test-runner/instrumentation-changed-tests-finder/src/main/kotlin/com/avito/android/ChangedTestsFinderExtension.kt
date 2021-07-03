package com.avito.android

import org.gradle.api.provider.Property

public abstract class ChangedTestsFinderExtension {

    public abstract val targetCommit: Property<String>
}
