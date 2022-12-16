package com.avito.android.tech_budget.internal.di

import com.avito.android.OwnerSerializer
import com.avito.android.owner.adapter.DefaultOwnerAdapter
import com.avito.android.owner.adapter.OwnerAdapter
import com.avito.logger.LoggerFactory
import retrofit2.create

internal class ApiServiceProvider(
    private val baseUrl: String,
    private val ownerAdapter: OwnerAdapter,
    private val loggerFactory: LoggerFactory
) {

    constructor(
        baseUrl: String,
        ownerSerializer: OwnerSerializer,
        loggerFactory: LoggerFactory
    ) : this(baseUrl, DefaultOwnerAdapter(ownerSerializer), loggerFactory)

    inline fun <reified S> provide(): S =
        RetrofitProvider(baseUrl, MoshiProvider(ownerAdapter), loggerFactory)
            .provide().create()
}
