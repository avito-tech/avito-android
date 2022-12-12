package com.avito.android.tech_budget.internal.deeplinks.serializer

import com.avito.android.OwnerSerializer
import com.avito.android.owner.adapter.DefaultOwnerAdapter
import com.avito.android.tech_budget.deeplinks.DeepLink
import com.avito.android.tech_budget.deeplinks.DeepLinksFileParser
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.squareup.moshi.adapter
import java.io.File

internal class JsonDeepLinksFileParser(ownerSerializer: OwnerSerializer) : DeepLinksFileParser {

    @OptIn(ExperimentalStdlibApi::class)
    private val jsonAdapter by lazy {
        val ownersAdapter = DefaultOwnerAdapter(ownerSerializer)
        val moshi = MoshiProvider(ownersAdapter).provide()
        moshi.adapter<List<DeepLink>>()
    }

    override fun parse(file: File): List<DeepLink> {
        val rawText = file.readText()
        return requireNotNull(jsonAdapter.fromJson(rawText)) {
            "Error during DeepLink model deserialization! Raw test: $rawText"
        }
    }
}
