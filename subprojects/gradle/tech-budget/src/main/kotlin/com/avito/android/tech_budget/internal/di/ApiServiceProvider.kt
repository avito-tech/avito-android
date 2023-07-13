package com.avito.android.tech_budget.internal.di

import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.logger.LoggerFactory
import retrofit2.create

internal class ApiServiceProvider(
    private val baseUrl: String,
    private val loggerFactory: LoggerFactory,
    private val ownerAdapterFactory: OwnerAdapterFactory = OwnerAdapterFactory(),
) {

    inline fun <reified S> provide(): S {
        val retrofitProvider = RetrofitProvider(
            baseUrl,
            MoshiProvider(ownerAdapterFactory),
            loggerFactory
        )

        return retrofitProvider.provide().create()
    }
}
