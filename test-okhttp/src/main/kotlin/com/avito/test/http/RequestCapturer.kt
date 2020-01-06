package com.avito.test.http

import com.google.common.truth.Truth
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.core.StringContains

private typealias Header = Pair<String, String>

class RequestCapturer(val requestMatcher: RecordedRequest.() -> Boolean) {

    private val requests = mutableListOf<RecordedRequest>()

    fun capture(recordedRequest: RecordedRequest) {
        synchronized(this) {
            requests.add(recordedRequest)
        }
    }

    val checks = Checks()

    inner class Checks {
        fun singleRequestCaptured(): RequestChecks {
            return synchronized(this@RequestCapturer) {
                Truth.assertWithMessage("request size")
                    .that(requests.size)
                    .isEqualTo(1)
                RequestChecks(requests.first())
            }
        }
    }

    inner class RequestChecks(private val recordedRequest: RecordedRequest) {
        private val body: String by lazy {
            val readUtf8 = recordedRequest.body.readUtf8()
            println("captured request (${recordedRequest.path}) body: $readUtf8")
            readUtf8
        }

        private val headers: List<Header> by lazy {
            val headers = recordedRequest.headers
            headers.names().map { name -> name to headers.get(name)!! }
        }

        fun pathContains(substring: String): RequestChecks {
            MatcherAssert.assertThat(recordedRequest.path, StringContains(substring))
            return this
        }

        fun bodyContains(substring: String): RequestChecks {
            Truth.assertThat(body).contains(substring)
            return this
        }

        fun bodyDoesntContain(substring: String): RequestChecks {
            Truth.assertThat(body).doesNotContain(substring)
            return this
        }

        fun containsHeader(name: String, value: String): RequestChecks {
            Truth.assertThat(headers).contains(name to value)
            return this
        }

        fun bodyMatches(matcher: Matcher<Any?>): RequestChecks {
            MatcherAssert.assertThat(body, matcher)
            return this
        }
    }
}
