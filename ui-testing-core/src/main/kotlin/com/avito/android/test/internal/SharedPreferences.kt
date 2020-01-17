package com.avito.android.test.internal

import android.content.Context
import java.io.File

internal class SharedPreferences(private val appContext: Context) {

    fun clear() {
        sharedPreferencesFileNames().forEach { sharedPreferenceFileName ->
            @Suppress("Missing commit() on SharedPreference editor")
            appContext.getSharedPreferences(sharedPreferenceFileName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
        }
    }

    private fun sharedPreferencesLocation(): File {
        return File(appContext.filesDir.parentFile, "shared_prefs")
    }

    private fun sharedPreferencesFileNames(): List<String> {
        return sharedPreferencesLocation()
            .list()
            ?.map { it.replace(".xml", "") }
            ?: emptyList()
    }
}
