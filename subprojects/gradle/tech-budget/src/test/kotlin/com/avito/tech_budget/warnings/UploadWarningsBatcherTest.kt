package com.avito.tech_budget.warnings

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.warnings.upload.UploadWarningsApi
import com.avito.android.tech_budget.internal.warnings.upload.UploadWarningsBatcher
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import com.avito.android.tech_budget.internal.warnings.upload.model.WarningsRequestBody
import com.avito.composite_exception.CompositeException
import com.google.common.truth.Truth
import okhttp3.Request
import okio.Timeout
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

internal class UploadWarningsBatcherTest {

    private val fakeApiClient = FakeUploadWarningsApi(FakeCall())
    private val batchSize = 5
    private val parallelRequestsCount = 4
    private val batcher = UploadWarningsBatcher(
        batchSize = batchSize,
        parallelRequestsCount = parallelRequestsCount,
        apiClient = fakeApiClient
    )

    @Test
    fun `send large amount of warnings - warnings are sent as batches`() {

        batcher.send(
            dumpInfo = fakeDumpInfo(),
            warnings = createWarnings(count = 22)
        )

        val expectedRequestsCount = 5 // 5, 10, 15, 20, 22

        Truth
            .assertThat(fakeApiClient.requests)
            .hasSize(expectedRequestsCount)

        fakeApiClient.requests.map { it.detektIssues }.forEach { uploadedWarnings ->
            Truth
                .assertThat(uploadedWarnings.size)
                .isAtMost(batchSize)
        }
    }

    @Test
    fun `send large amount of warnings - max parallel requests sent to a server `() {

        batcher.send(
            dumpInfo = fakeDumpInfo(),
            warnings = createWarnings(count = 22)
        )

        Truth
            .assertThat(fakeApiClient.usedThreads)
            .hasSize(parallelRequestsCount)
    }

    @Test
    fun `send little amount of warnings - everything is sent in one request`() {
        batcher.send(
            dumpInfo = fakeDumpInfo(),
            warnings = createWarnings(batchSize - 1)
        )

        Truth
            .assertThat(fakeApiClient.usedThreads)
            .hasSize(1)
        Truth
            .assertThat(fakeApiClient.requests)
            .hasSize(1)
    }

    @Test
    fun `send warnings with error - message displayed`() {
        fakeApiClient.hasError.set(true)

        val error = assertThrows<CompositeException> {
            batcher.send(
                dumpInfo = fakeDumpInfo(),
                warnings = createWarnings(22)
            )
        }
        Truth
            .assertThat(error.message)
            .contains("5 errors occurred")
    }

    private fun createWarnings(count: Int) = mutableListOf<Warning>().apply {
        repeat(count) { i ->
            add(Warning("/test.kt", ":app", "Warning $i", "group", "rule", 10))
        }
    }

    private fun fakeDumpInfo() = DumpInfo("", "", "avito")

    private class FakeUploadWarningsApi(private val call: Call<DumpResponse>) : UploadWarningsApi {

        var hasError = AtomicBoolean(false)

        val requests: MutableList<WarningsRequestBody> = CopyOnWriteArrayList()
        val usedThreads: MutableSet<String> = CopyOnWriteArraySet()

        override fun dumpWarnings(request: WarningsRequestBody): Call<DumpResponse> {
            if (hasError.get()) error("Fake dump error")
            requests += request
            usedThreads += Thread.currentThread().name
            return call
        }
    }

    private class FakeCall : Call<DumpResponse> {

        override fun clone(): Call<DumpResponse> {
            return FakeCall()
        }

        override fun execute(): Response<DumpResponse> {
            return Response.success(null)
        }

        override fun enqueue(callback: Callback<DumpResponse>) {
        }

        override fun isExecuted(): Boolean {
            return true
        }

        override fun cancel() {
        }

        override fun isCanceled(): Boolean {
            return false
        }

        override fun request(): Request {
            error("Fake implementation")
        }

        override fun timeout(): Timeout {
            error("Fake implementation")
        }
    }
}
