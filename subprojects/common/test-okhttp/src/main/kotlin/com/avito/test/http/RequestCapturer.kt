package com.avito.test.http

import com.avito.android.util.waitForAssertion
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains

public class RequestCapturer(
    public val requestMatcher: RequestData.() -> Boolean
) {

    private val requests = mutableListOf<RecordedRequest>()

    public val checks: Checks = Checks()

    public fun capture(recordedRequest: RecordedRequest): Unit = synchronized(this) {
        requests.add(recordedRequest)
    }

    public inner class Checks {

        public fun singleRequestCaptured(): RequestChecks = waitForAssertion {
            synchronized(this@RequestCapturer) {
                assertThat("", requests.size == 1)
                RequestChecks(RequestData(requests.first()))
            }
        }
    }

    public inner class RequestChecks(private val requestData: RequestData) {

        public val formUrlEncodedBody: FormUrlEncodedBodyChecks = FormUrlEncodedBodyChecks(
            recordedRequest = requestData.recordedRequest,
            stringBody = requestData.body
        )

        public fun pathContains(substring: String): RequestChecks {
            assertThat(requestData.path, StringContains(substring))
            return this
        }

        public fun bodyContains(vararg substrings: String): RequestChecks {
            substrings.forEach {
                waitForAssertion {
                    assertThat(requestData.body, StringContains(it))
                }
            }
            return this
        }

        public fun bodyDoesntContain(substring: String): RequestChecks {
            assertThat(requestData.body).doesNotContain(substring)
            return this
        }

        public fun containsHeader(name: String, value: String): RequestChecks {
            assertThat(requestData.headers).contains(name to value)
            return this
        }

        public fun bodyMatches(matcher: Matcher<Any?>): RequestChecks {
            assertThat(requestData.body, matcher)
            return this
        }
    }
}
