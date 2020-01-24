package com.avito.android.mock

import android.annotation.SuppressLint
import android.util.Log
import com.avito.android.mock.FormUrlEncodedBodyChecks.Result.KeyNotFound
import com.avito.android.mock.FormUrlEncodedBodyChecks.Result.NoProblem
import com.avito.android.mock.FormUrlEncodedBodyChecks.Result.ValueDoesNotMatch
import com.avito.android.util.waitForAssertion
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains
import ru.avito.util.Is
import java.net.URLDecoder
import java.net.URLEncoder

class RequestCapturer(val requestMatcher: RequestData.() -> Boolean) {

    private val requests = mutableListOf<RecordedRequest>()

    fun capture(recordedRequest: RecordedRequest) = synchronized(this) {
        requests.add(recordedRequest)
    }

    val checks = Checks()

    inner class Checks {

        fun singleRequestCaptured(): RequestChecks = waitForAssertion {
            synchronized(this@RequestCapturer) {
                assertThat("", requests.size == 1)
                RequestChecks(requests.first())
            }
        }
    }

    @SuppressLint("LogNotTimber")
    inner class RequestChecks(private val recordedRequest: RecordedRequest) {

        private val body: String by lazy {
            val readUtf8 = recordedRequest.body.readUtf8()
            Log.d(
                "MOCK_WEB_SERVER",
                "captured request (${recordedRequest.path}) body: $readUtf8"
            )
            readUtf8
        }

        val formUrlEncodedBody: FormUrlEncodedBodyChecks =
            FormUrlEncodedBodyChecks(recordedRequest, body)

        fun bodyContains(vararg substrings: String) {
            substrings.forEach {
                waitForAssertion {
                    assertThat(body, StringContains(it))
                }
            }
        }
    }
}

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
                assertThat(keyValue.size, Is(2))
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
                errorMessage.appendln()
                errorMessage.appendln("Request should contain these keys, but its not: $keyErrors")
                errorMessage.appendln("Actual request keys are:" +
                    actualParameters.keys.joinToString(separator = ", ") { decode(it) }
                )
            }

            val valueErrors = results.asSequence()
                .filterIsInstance<ValueDoesNotMatch>()
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
