package com.avito.android.plugin.internal

import io.github.detekt.parser.createKotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

internal class ElementsFactory {

    private val environment: KotlinCoreEnvironment = createKotlinCoreEnvironment()
    private val psiFactory = KtPsiFactory(environment.project, markGenerated = true)

    fun createImportDirective(clazz: String, alias: String? = null): KtImportDirective {
        val aliasIdentifier = if (alias == null) null else Name.identifier(alias)
        return psiFactory.createImportDirective(
            ImportPath(FqName(clazz), isAllUnder = false, alias = aliasIdentifier)
        )
    }

    fun createNewLine(): PsiElement = psiFactory.createWhiteSpace("\n")

    fun createExpression(text: String): KtExpression = psiFactory.createExpression(text)
}
