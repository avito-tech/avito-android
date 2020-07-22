package com.avito.android.plugin

import io.github.detekt.parser.KtCompiler
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

// TODO: Find a way to create a PsiElement without file compilation (like in PsiJavaParserFacade)
internal fun KtCompiler.createKtFile(content: String): KtFile {
    val tempFile = File.createTempFile("synthetic-", ".kt")
    tempFile.deleteOnExit()
    tempFile.writeText(content)

    return compile(tempFile.toPath(), tempFile.toPath())
}

internal fun KtCompiler.parse(file: File): KtFile = compile(file.toPath(), file.toPath())
