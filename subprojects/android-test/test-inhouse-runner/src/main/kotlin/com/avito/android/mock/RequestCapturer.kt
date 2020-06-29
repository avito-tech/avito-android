package com.avito.android.mock

import android.annotation.SuppressLint
import android.util.Log
import com.avito.android.util.waitForAssertion
import com.avito.test.http.FormUrlEncodedBodyChecks
import com.avito.test.http.RequestData
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains

@Deprecated(
    "use same class from common module",
    replaceWith = ReplaceWith("com.avito.test.http.RequestCapturer")
)
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

