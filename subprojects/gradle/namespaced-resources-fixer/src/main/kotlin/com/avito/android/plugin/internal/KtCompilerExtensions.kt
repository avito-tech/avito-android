package com.avito.android.plugin.internal

import io.github.detekt.parser.KtCompiler
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

internal fun KtCompiler.parse(file: File): KtFile = compile(file.toPath(), file.toPath())
