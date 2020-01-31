package com.avito.android.lint.dependency

import org.gradle.api.artifacts.component.ComponentIdentifier

internal sealed class SuspiciousDependency(val component: ComponentIdentifier) {
    class Unused(component: ComponentIdentifier) : SuspiciousDependency(component)
    class UsedTransitively(
        component: ComponentIdentifier,
        val transitiveComponents: Set<ComponentIdentifier>
    ) : SuspiciousDependency(component)
}
