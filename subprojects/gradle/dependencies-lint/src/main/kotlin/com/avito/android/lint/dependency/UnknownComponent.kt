package com.avito.android.lint.dependency

import org.gradle.api.artifacts.component.ComponentIdentifier

internal class UnknownComponent(val description: String): ComponentIdentifier {
    override fun getDisplayName(): String {
        return "Unknown dependency: $description"
    }
}
