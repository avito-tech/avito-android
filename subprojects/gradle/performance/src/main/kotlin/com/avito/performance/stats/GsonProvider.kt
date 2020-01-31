package com.avito.performance.stats

import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal class GsonProvider {

    fun getGson(): Gson = GsonBuilder().create()

}
