package com.avito.report

import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.internal.model.ListRpcResponse
import com.avito.report.internal.model.RfcRpcRequest
import com.avito.report.model.HistoryListResult
import org.funktionale.tries.Try

internal class ReportsHistoryApiImpl(private val requestProvider: JsonRpcRequestProvider) : ReportsHistoryApi {

    override fun getTestHistory(testName: String, branch: String, depth: Int): Try<HistoryListResult> {
        return Try {
            requestProvider.jsonRpcRequest<ListRpcResponse>(
                RfcRpcRequest(
                    method = "History.GetByTest",
                    params = mapOf(
                        "test_name" to testName,
                        "tags_filter" to listOf(
                            "buildBranch:$branch",
                            "\$status:1" //only valid tests, $ means it is not a tag, but a field
                        ),
                        "depth_limit" to depth,
                        "data_set_num" to -1 //always need to be here
                    )
                )
            ).result
        }
    }
}
