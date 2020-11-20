package com.avito.android.plugin.build_param_check.incremental_check

import java.util.Locale
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

internal class FakeProcessingEnvironment(
    private val options: Map<String, String> = emptyMap(),
    private val logTag: String = ""
) : ProcessingEnvironment {
    override fun getElementUtils(): Elements? = supposeNotUsed()

    override fun getTypeUtils(): Types? = supposeNotUsed()

    override fun getMessager(): Messager = PrintStreamMessager(tag = logTag)

    override fun getLocale(): Locale = Locale.getDefault()

    override fun getSourceVersion(): SourceVersion = SourceVersion.RELEASE_8

    override fun getOptions(): MutableMap<String, String> = options.toMutableMap()

    override fun getFiler(): Filer? = supposeNotUsed()

    private fun <T> supposeNotUsed(): T? = null
}
