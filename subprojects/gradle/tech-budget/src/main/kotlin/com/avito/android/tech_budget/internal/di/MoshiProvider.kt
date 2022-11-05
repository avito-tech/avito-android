package com.avito.android.tech_budget.internal.di

import com.squareup.moshi.Moshi

internal class MoshiProvider {

    fun provide(): Moshi = Moshi.Builder().build()
}
