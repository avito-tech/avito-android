package com.avito.report.internal.model

/**
 * @param id we use jsonrpc over http, so no need to match ids, its for async calls (e.g.websocket)
 */
internal data class RfcRpcRequest(
    val id: Int = 1,
    val method: String,
    val jsonrpc: String = "2.0",
    val params: Map<String, Any>
)
