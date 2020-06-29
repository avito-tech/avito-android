package com.avito.android.mock

import com.avito.test.http.FormUrlEncodedBodyChecks
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.MatcherAssert
import ru.avito.util.Is
import java.net.URLDecoder
import java.net.URLEncoder

class FormUrlEncodedBodyChecks(
    private val recordedRequest: RecordedRequest,
    private val stringBody: String
) {

    sealed class Result {
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
                    Is(2)
                )
                Pair(keyValue[0], keyValue[1])
            }
            .toMap()
    }

    fun contains(vararg parameters: Pair<String, String>) {
        val results = parameters.map { (key, value) ->
            val encodedKey = encode(key)
            if (actualParameters.containsKey(encodedKey)) {
                val actualValue = actualParameters[encodedKey]
                val encodedValue: String = encode(value)
                if (actualValue != encodedValue) {
                    FormUrlEncodedBodyChecks.Result.ValueDoesNotMatch(
                        key,
                        value,
                        actualValue?.let { decode(it) })
                } else {
                    FormUrlEncodedBodyChecks.Result.NoProblem
                }
            } else {
                FormUrlEncodedBodyChecks.Result.KeyNotFound(key)
            }
        }

        if (results.any { it !is FormUrlEncodedBodyChecks.Result.NoProblem }) {
            val errorMessage =
                StringBuilder("Recorded request assertion failed: ${recordedRequest.path}\n")

            val keyErrors = results.asSequence()
                .filterIsInstance<FormUrlEncodedBodyChecks.Result.KeyNotFound>()
                .joinToString(separator = ", ") { it.key }

            if (keyErrors.isNotBlank()) {
                errorMessage.appendln()
                errorMessage.appendln("Request should contain these keys, but its not: $keyErrors")
                errorMessage.appendln("Actual request keys are:" +
                    actualParameters.keys.joinToString(separator = ", ") { decode(it) }
                )
            }

            val valueErrors = results.asSequence()
                .filterIsInstance<FormUrlEncodedBodyChecks.Result.ValueDoesNotMatch>()
                .joinToString(separator = "\n") { "[${it.key}] expected: ${it.value}; actual: ${it.actualValue}" }

            if (valueErrors.isNotBlank()) {
                errorMessage.appendln()
                errorMessage.appendln("Actual request body values differs from what was expected:")
                errorMessage.append(valueErrors)
            }

            throw AssertionError(errorMessage.toString())
        }
    }

    private fun encode(string: String): String = URLEncoder.encode(string, "UTF-8")

    private fun decode(string: String): String = URLDecoder.decode(string, "UTF-8")
}
