package com.avito.android.tech_budget.internal.feature_toggles.serializer

import com.avito.android.OwnerSerializer
import com.avito.android.owner.adapter.DefaultOwnerAdapter
import com.avito.android.tech_budget.feature_toggles.FeatureToggle
import com.avito.android.tech_budget.feature_toggles.FeatureTogglesFileParser
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.squareup.moshi.adapter
import java.io.File

internal class JsonFeatureToggleFileParser(ownerSerializer: OwnerSerializer) : FeatureTogglesFileParser {

    @OptIn(ExperimentalStdlibApi::class)
    private val jsonAdapter by lazy {
        val ownersAdapter = DefaultOwnerAdapter(ownerSerializer)
        val moshi = MoshiProvider(ownersAdapter).provide()
        moshi.adapter<List<FeatureToggle>>()
    }

    override fun parse(file: File): List<FeatureToggle> {
        val rawText = file.readText()
        return requireNotNull(jsonAdapter.fromJson(rawText)) {
            "Error during feature toggle model deserialization! Raw toggle: $rawText"
        }
    }
}
