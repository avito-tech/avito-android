package com.avito.android.tech_budget.internal.utils.parser

import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.avito.android.tech_budget.parser.FileParser
import com.squareup.moshi.Types
import java.io.File
import kotlin.reflect.KClass

internal class JsonFileParser<T : Any>(ownersAdapter: OwnerAdapterFactory, elementType: KClass<T>) : FileParser<T> {

    private val jsonAdapter by lazy {
        val moshi = MoshiProvider(ownersAdapter).provide()
        val listType = Types.newParameterizedType(List::class.java, elementType.java)
        moshi.adapter<List<T>>(listType)
    }

    override fun parse(file: File): List<T> {
        val rawText = file.readText()
        return requireNotNull(jsonAdapter.fromJson(rawText)) {
            "Error during model deserialization! Raw test: $rawText"
        }
    }
}
