package com.avito.android.tech_budget.internal.ab_tests.serializer

import com.avito.android.OwnerSerializer
import com.avito.android.owner.adapter.DefaultOwnerAdapter
import com.avito.android.tech_budget.ab_tests.ABTest
import com.avito.android.tech_budget.ab_tests.ABTestsFileParser
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.squareup.moshi.adapter
import java.io.File

internal class JsonABTestFileParser(ownerSerializer: OwnerSerializer) : ABTestsFileParser {

    @OptIn(ExperimentalStdlibApi::class)
    private val jsonAdapter by lazy {
        val ownersAdapter = DefaultOwnerAdapter(ownerSerializer)
        val moshi = MoshiProvider(ownersAdapter).provide()
        moshi.adapter<List<ABTest>>()
    }

    override fun parse(file: File): List<ABTest> {
        val rawABTests = file.readText()
        return requireNotNull(jsonAdapter.fromJson(rawABTests)) {
            "Error during AB Test model deserialization! Raw test: $rawABTests"
        }
    }
}
