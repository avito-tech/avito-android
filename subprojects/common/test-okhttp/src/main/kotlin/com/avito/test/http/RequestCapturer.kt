package com.avito.test.http

import com.avito.android.util.waitForAssertion
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains

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
                RequestChecks(RequestData(requests.first()))
            }
        }
    }

    inner class RequestChecks(private val requestData: RequestData) {

        val formUrlEncodedBody: FormUrlEncodedBodyChecks = FormUrlEncodedBodyChecks(
            recordedRequest = requestData.recordedRequest,
            stringBody = requestData.body
        )

        fun pathContains(substring: String): RequestChecks {
            assertThat(requestData.path, StringContains(substring))
            return this
        }

        fun bodyContains(vararg substrings: String): RequestChecks {
            substrings.forEach {
                waitForAssertion {
                    assertThat(requestData.body, StringContains(it))
                }
            }
            return this
        }

        fun bodyDoesntContain(substring: String): RequestChecks {
            assertThat(requestData.body).doesNotContain(substring)
            return this
        }

        fun containsHeader(name: String, value: String): RequestChecks {
            assertThat(requestData.headers).contains(name to value)
            return this
        }

        fun bodyMatches(matcher: Matcher<Any?>): RequestChecks {
            assertThat(requestData.body, matcher)
            return this
        }
    }
}
