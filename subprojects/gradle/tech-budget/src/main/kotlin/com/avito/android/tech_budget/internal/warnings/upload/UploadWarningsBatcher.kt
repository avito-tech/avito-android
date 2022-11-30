package com.avito.android.tech_budget.internal.warnings.upload

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import com.avito.android.tech_budget.internal.warnings.upload.model.WarningsRequestBody
import com.avito.composite_exception.CompositeException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

internal class UploadWarningsBatcher(
    private val batchSize: Int,
    parallelRequestsCount: Int,
    private val apiClient: UploadWarningsApi
) {

    private val warningsUploadExecutor = Executors.newFixedThreadPool(parallelRequestsCount)
    private val requestErrors = CopyOnWriteArrayList<Throwable>()

    fun send(dumpInfo: DumpInfo, warnings: List<Warning>) {

        val batches = warnings.chunked(batchSize)
        val uploadJobs = batches.map { batch ->
            warningsUploadExecutor.submit {
                sendWarningsBatch(dumpInfo, batch)
            }
        }
        uploadJobs.forEach { it.get() }
        warningsUploadExecutor.shutdownNow()
        if (requestErrors.isNotEmpty()) {
            throw CompositeException(
                message = "${requestErrors.size} errors occurred during dumpWarnings request.",
                throwables = requestErrors.toTypedArray()
            )
        }
    }

    private fun sendWarningsBatch(dumpInfo: DumpInfo, batch: List<Warning>) {
        try {
            apiClient.dumpWarnings(WarningsRequestBody(dumpInfo, batch))
                .executeWithHttpFailure(errorMessage = "Upload warnings request failed")
        } catch (error: Throwable) {
            requestErrors.add(error)
        }
    }

    companion object {
        const val DEFAULT_BATCH_SIZE = 1000
        const val DEFAULT_PARALLEL_REQUESTS_COUNT = 16
    }
}
