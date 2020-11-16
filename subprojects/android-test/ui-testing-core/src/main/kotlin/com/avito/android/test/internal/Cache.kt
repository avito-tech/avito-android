package com.avito.android.test.internal

import android.content.Context
import com.avito.android.waiter.waitFor
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import java.io.File

internal class Cache(private val appContext: Context) {

    fun clear() {
        val cacheDir = appContext.cacheDir
        if (cacheDir.list() != null) {
            waitFor(frequencyMs = DELETE_FREQUENCY_MS, timeoutMs = DELETE_TIMEOUT_MS) {
                assertThat(
                    "Can't delete ${cacheDir.path}",
                    deleteRecursive(cacheDir),
                    `is`(true)
                )
            }
        }
    }

    private fun deleteRecursive(directory: File, vararg excludes: String): Boolean {
        if (excludes.isNotEmpty() && listOf(*excludes).contains(directory.name)) {
            return true
        }

        if (directory.isDirectory) {
            directory.list()?.forEach { content ->
                waitFor(frequencyMs = DELETE_FREQUENCY_MS, timeoutMs = DELETE_TIMEOUT_MS) {
                    assertThat(
                        "Can't delete file $content in ${directory.path}",
                        deleteRecursive(File(directory, content), *excludes),
                        `is`(true)
                    )
                }
            }
        }
        return directory.delete()
    }
}

private const val DELETE_FREQUENCY_MS = 500L
private const val DELETE_TIMEOUT_MS = 5000L
