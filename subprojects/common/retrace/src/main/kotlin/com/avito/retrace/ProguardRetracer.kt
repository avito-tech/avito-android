package com.avito.retrace

import proguard.retrace.ReTrace
import java.io.File
import java.io.LineNumberReader
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter

interface ProguardRetracer {

    fun retrace(content: String): String

    class Impl(
        private val mappings: List<File>
    ) : ProguardRetracer {

        override fun retrace(content: String): String {
            var result = content
            mappings.map { ReTrace(ReTrace.STACK_TRACE_EXPRESSION, false, it) }
                .forEach { result = it.retrace(result) }
            return result
        }

        private fun ReTrace.retrace(content: String): String {
            val resultWriter = StringWriter()
            LineNumberReader(StringReader(content)).use { stackTraceReader ->
                PrintWriter(resultWriter).use { stackTraceWriter ->
                    retrace(stackTraceReader, stackTraceWriter)
                }
            }
            return resultWriter.toString()
        }
    }

}