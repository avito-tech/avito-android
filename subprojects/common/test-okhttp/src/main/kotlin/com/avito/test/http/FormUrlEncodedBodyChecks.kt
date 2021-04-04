package com.avito.test.http

import com.avito.test.http.FormUrlEncodedBodyChecks.Result.KeyNotFound
import com.avito.test.http.FormUrlEncodedBodyChecks.Result.NoProblem
import com.avito.test.http.FormUrlEncodedBodyChecks.Result.ValueDoesNotMatch
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import java.net.URLDecoder
import java.net.URLEncoder

public class FormUrlEncodedBodyChecks(
    private val recordedRequest: RecordedRequest,
    private val stringBody: String
) {

    private sealed class Result {
        object NoProblem : Result()
        data class KeyNotFound(val key: String) : Result()
        data class ValueDoesNotMatch(val key: String, val value: String, val actualValue: String?) :
            Result()
    }

    private val actualParameters by lazy {
        stringBody.splitToSequence('&')
            .map {
                val keyValue = it.split('=')
                MatcherAssert.assertThat(
                    keyValue.size,
                    Is.`is`(2)
                )
                Pair(keyValue[0], keyValue[1])
            }
            .toMap()
    }

    public fun contains(vararg parameters: Pair<String, String>) {
        val results = parameters.map { (key, value) ->
            val encodedKey = encode(key)
            if (actualParameters.containsKey(encodedKey)) {
                val actualValue = actualParameters[encodedKey]
                val encodedValue: String = encode(value)
                if (actualValue != encodedValue) {
                    ValueDoesNotMatch(key, value, actualValue?.let { decode(it) })
                } else {
                    NoProblem
                }
            } else {
                KeyNotFound(key)
            }
        }

        if (results.any { it !is NoProblem }) {
            val errorMessage =
                StringBuilder("Recorded request assertion failed: ${recordedRequest.path}\n")

            val keyErrors = results.asSequence()
                .filterIsInstance<KeyNotFound>()
                .joinToString(separator = ", ") { it.key }

            if (keyErrors.isNotBlank()) {
                errorMessage.appendLine()
                errorMessage.appendLine("Request should contain these keys, but its not: $keyErrors")
                errorMessage.appendLine(
                    "Actual request keys are:" +
                        actualParameters.keys.joinToString(separator = ", ") { decode(it) }
                )
            }

            val valueErrors = results.asSequence()
                .filterIsInstance<ValueDoesNotMatch>()
                .joinToString(separator = "\n") { "[${it.key}] expected: ${it.value}; actual: ${it.actualValue}" }

            if (valueErrors.isNotBlank()) {
                errorMessage.appendLine()
                errorMessage.appendLine("Actual request body values differs from what was expected:")
                errorMessage.append(valueErrors)
            }

            throw AssertionError(errorMessage.toString())
        }
    }

    private fun encode(string: String): String = URLEncoder.encode(string, "UTF-8")

    private fun decode(string: String): String = URLDecoder.decode(string, "UTF-8")
}
