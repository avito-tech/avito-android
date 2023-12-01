package com.avito.android.baseline_profile.configuration

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

public abstract class ApplyBaselineProfileExtension {
    internal abstract val taskConfiguration: NamedDomainObjectContainer<ApplyBaselineProfileConfiguration>

    public fun taskConfiguration(action: Action<NamedDomainObjectContainer<ApplyBaselineProfileConfiguration>>) {
        action.execute(taskConfiguration)
    }
}
