package com.avito.instrumentation.impact

import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Key
import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtilRt
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.nio.file.Path

class KotlinCompiler(
    private val project: Project,
    private val psiFileFactory: PsiFileFactory = PsiFileFactory.getInstance(project)
) {

    fun compile(file: File): KtFile {
        val content = file.readText()
        val lineSeparator = content.determineLineSeparator()
        val normalizedContent = StringUtilRt.convertLineSeparators(content)
        val ktFile = createKtFile(normalizedContent, file.absoluteFile.toPath())

        return ktFile.apply {
            putUserData(LINE_SEPARATOR, lineSeparator)
        }
    }

    private fun createKtFile(content: String, path: Path) = psiFileFactory.createFileFromText(
        path.fileName.toString(),
        KotlinLanguage.INSTANCE,
        StringUtilRt.convertLineSeparators(content),
        true,
        true,
        false,
        LightVirtualFile(path.toString())
    ) as KtFile

    private fun String.determineLineSeparator(): String {
        val i = this.lastIndexOf('\n')
        if (i == -1) {
            return if (this.lastIndexOf('\r') == -1) System.getProperty("line.separator") else "\r"
        }
        return if (i != 0 && this[i] == '\r') "\r\n" else "\n"
    }

    companion object {
        val LINE_SEPARATOR: Key<String> = Key("lineSeparator")
    }
}
