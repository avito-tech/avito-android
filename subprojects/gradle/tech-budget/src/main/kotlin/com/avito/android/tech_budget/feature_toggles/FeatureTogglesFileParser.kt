package com.avito.android.tech_budget.feature_toggles

import java.io.File

public interface FeatureTogglesFileParser {
    public fun parse(file: File): List<FeatureToggle>
}
