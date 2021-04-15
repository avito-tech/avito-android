package com.avito.android.test.report

import com.avito.android.util.exhaustive
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import ru.avito.reporter.WebSocketReporter

class ReportViewerWebsocketReporter(
    private val report: Report
) : WebSocketReporter {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

    override fun onConnect(host: String) {
        report.addComment("WS: connect to $host")
    }

    override fun onSend(message: String, enqueued: Boolean) {
        val info = message.parseOutgoingMessage()

        when (info) {
            is OutgoingMessage.RpcRequest -> {
                val label = if (enqueued) {
                    "WS: Call id = ${info.id}, method = ${info.method}"
                } else {
                    "WS: Call wasn't enqueued: id = ${info.id}, method = ${info.method}"
                }
                report.addText(label, info.prettyJson)
            }
            is OutgoingMessage.Unknown -> {
                val label = if (enqueued) {
                    "WS: Call"
                } else {
                    "WS: Call wasn't enqueued"
                }
                report.addText(label, info.prettyJson)
            }
        }.exhaustive
    }

    override fun onReceive(message: String) {
        val info = message.parseIncomingMessage()

        when (info) {
            is IncomingMessage.RpcResponse -> {
                val label = if (info.isOk) {
                    "WS: Response id = ${info.id}"
                } else {
                    "WS: Response ERROR id = ${info.id}"
                }
                report.addText(label, info.prettyJson)
            }
            is IncomingMessage.Event -> {
                val label = "WS: Event id = ${info.id}, type = ${info.type}"
                report.addText(label, info.prettyJson)
            }
            is IncomingMessage.Unknown -> {
                val label = "WS: Message"
                report.addText(label, info.prettyJson)
            }
        }.exhaustive
    }

    override fun onError(message: String) {
        report.addComment("WS: failed – $message")
    }

    override fun onClose() {
        report.addComment("WS: closed")
    }

    private fun String.parseOutgoingMessage(): OutgoingMessage {
        val json = gson.fromJson<JsonElement>(this)
        val prettyJson = gson.toJson(json)

        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            val id = obj["id"]?.asString
            when {
                id != null && obj.has("jsonrpc") -> {
                    val method = obj["method"].asString
                    OutgoingMessage.RpcRequest(id, method, prettyJson)
                }
                else -> OutgoingMessage.Unknown(prettyJson)
            }
        } else {
            OutgoingMessage.Unknown(prettyJson)
        }
    }

    private fun String.parseIncomingMessage(): IncomingMessage {
        val json = gson.fromJson<JsonElement>(this)
        val prettyJson = gson.toJson(json)

        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            val id = obj["id"]?.asString
            when {
                id != null && obj.has("jsonrpc") -> {
                    val isOk = !obj.has("error") && obj.has("result")
                    IncomingMessage.RpcResponse(id, isOk, prettyJson)
                }
                obj.has("type") -> {
                    val type = obj["type"].asString
                    IncomingMessage.Event(id, type, prettyJson)
                }
                else -> IncomingMessage.Unknown(prettyJson)
            }
        } else {
            IncomingMessage.Unknown(prettyJson)
        }
    }

    private sealed class IncomingMessage {

        abstract val prettyJson: String

        class RpcResponse(val id: String, val isOk: Boolean, override val prettyJson: String) : IncomingMessage()
        class Event(val id: String?, val type: String, override val prettyJson: String) : IncomingMessage()
        class Unknown(override val prettyJson: String) : IncomingMessage()
    }

    private sealed class OutgoingMessage {

        abstract val prettyJson: String

        class RpcRequest(val id: String, val method: String, override val prettyJson: String) : OutgoingMessage()
        class Unknown(override val prettyJson: String) : OutgoingMessage()
    }
}
