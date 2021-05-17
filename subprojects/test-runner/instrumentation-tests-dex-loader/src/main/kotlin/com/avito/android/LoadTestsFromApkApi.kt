package com.avito.android

import com.avito.utils.createOrClear
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

private val gson: Gson = Gson()

internal fun Provider<RegularFile>.writeTestsInApk(tests: List<TestInApk>) {
    val file = get().asFile
    file.createOrClear()
    file.writeText(gson.toJson(tests))
}

fun Provider<RegularFile>.readTestsInApk(): List<TestInApk> {
    return gson.fromJson(get().asFile.reader())
}
