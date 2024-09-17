package com.avito.android.tech_budget

import com.avito.android.module_type.ModuleType
import com.avito.android.tech_budget.ab_tests.CollectABTestsConfiguration
import com.avito.android.tech_budget.deeplinks.CollectDeeplinksConfiguration
import com.avito.android.tech_budget.detekt.DetektConfiguration
import com.avito.android.tech_budget.feature_toggles.CollectFeatureTogglesConfiguration
import com.avito.android.tech_budget.perf_screen_owners.CollectPerfOwnersConfiguration
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class TechBudgetExtension {

    @get:Nested
    internal abstract val warnings: CollectWarningsConfiguration

    @get:Nested
    internal abstract val dumpInfo: DumpInfoConfiguration

    @get:Nested
    internal abstract val deepLinks: CollectDeeplinksConfiguration

    @get:Nested
    internal abstract val abTests: CollectABTestsConfiguration

    @get:Nested
    internal abstract val featureToggles: CollectFeatureTogglesConfiguration

    @get:Nested
    internal abstract val owners: CollectOwnersConfiguration

    @get:Nested
    internal abstract val perfOwners: CollectPerfOwnersConfiguration

    @get:Nested
    internal abstract val detekt: DetektConfiguration

    public abstract val compilationTimeFile: RegularFileProperty

    public abstract val getModuleFunctionalTypeName: Property<(ModuleType) -> String>

    public fun collectWarnings(action: Action<CollectWarningsConfiguration>) {
        action.execute(warnings)
    }

    public fun collectDeepLinks(action: Action<CollectDeeplinksConfiguration>) {
        action.execute(deepLinks)
    }

    public fun collectABTests(action: Action<CollectABTestsConfiguration>) {
        action.execute(abTests)
    }

    public fun collectFeatureToggles(action: Action<CollectFeatureTogglesConfiguration>) {
        action.execute(featureToggles)
    }

    public fun dumpInfo(action: Action<DumpInfoConfiguration>) {
        action.execute(dumpInfo)
    }

    public fun collectOwners(action: Action<CollectOwnersConfiguration>) {
        action.execute(owners)
    }

    public fun collectPerfOwners(action: Action<CollectPerfOwnersConfiguration>) {
        action.execute(perfOwners)
    }

    public fun detekt(action: Action<DetektConfiguration>) {
        action.execute(detekt)
    }
}

internal val Project.techBudgetExtension: TechBudgetExtension
    get() = rootProject.extensions.getByType(TechBudgetExtension::class.java)
