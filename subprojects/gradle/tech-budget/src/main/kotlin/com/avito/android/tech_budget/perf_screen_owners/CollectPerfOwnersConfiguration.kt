package com.avito.android.tech_budget.perf_screen_owners

import com.avito.android.tech_budget.parser.FileParser
import org.gradle.api.provider.Property

public abstract class CollectPerfOwnersConfiguration {

    public abstract val collectProjectPerfOwnersTaskName: Property<String>

    public abstract val screenInfoFileParser: Property<FileParser<PerformanceScreenInfo>>
}
