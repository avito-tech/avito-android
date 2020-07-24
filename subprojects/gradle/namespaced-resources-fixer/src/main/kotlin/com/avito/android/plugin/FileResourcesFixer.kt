package com.avito.android.plugin

import com.avito.android.plugin.internal.ElementsFactory
import com.avito.android.plugin.internal.findImport
import com.avito.android.plugin.internal.parse
import io.github.detekt.parser.KtCompiler
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import java.io.File

/**
 * Replace unambiguous R class references to Application by libraries R classes
 */
internal class FileResourcesFixer {

    private val compiler = KtCompiler()
    private val elementsFactory = ElementsFactory()

    /**
     * @return new file content or null if nothing changed
     */
    fun fixMergedResources(file: File, app: AndroidModule, libraries: List<AndroidModule>): String? {
        val ktFile = compiler.parse(file)

        val mergedRClassImport = findMergedRImport(ktFile, app) ?: return null

        val mergedRClassReferencesFinder = MergedRClassReferencesFinder(mergedRClassImport)
        mergedRClassReferencesFinder.visitKtFile(ktFile)

        val references = mergedRClassReferencesFinder.references
        if (references.isEmpty()) return null

        val uniqueResources: List<ResourceId> = findUniqueResources(libraries) // TODO: support string, drawable, ...

        replaceReferences(ktFile, references, uniqueResources)

        return ktFile.text
    }

    private fun replaceReferences(
        file: KtFile,
        rClassReferences: List<PsiElement>,
        uniqueResources: List<ResourceId>
    ) {
        val imports = requireNotNull(file.importList)

        rClassReferences.forEach { refElement ->
            val resource = uniqueResources.find { resource ->
                refElement.text.endsWith(".id.${resource.id}")
            }
            if (resource != null) {
                val replacement = createReplacementForResource(imports, resource)
                refElement.replace(replacement.element)
                if (replacement.importDirective != null) {
                    imports.add(elementsFactory.createNewLine())
                    imports.add(replacement.importDirective)
                }
            }
        }
    }

    private class MergedRClassReferencesFinder(
        private val classImport: KtImportDirective
    ) : KtTreeVisitorVoid() {

        var references = mutableListOf<PsiElement>()

        override fun visitElement(element: PsiElement) {
            if (element.isReferenceTo(classImport)) {
                references.add(element)
            }
            super.visitElement(element)
        }

        private fun PsiElement.isReferenceTo(importElement: KtImportDirective): Boolean {
            val element = this
            val isDotReferenceExpression = element is KtDotQualifiedExpression
            val partialDotReferenceExpression = element.context is KtDotQualifiedExpression
            val insideImport = element.context is KtImportDirective

            return isDotReferenceExpression
                && !insideImport
                && !partialDotReferenceExpression
                && element.text.startsWith(importElement.referencedClass + ".")
        }
    }

    private fun findMergedRImport(file: KtFile, app: AndroidModule): KtImportDirective? {
        val imports: List<KtImportDirective> = file.importList?.imports?.toList() ?: return null
        return imports.firstOrNull {
            it.importPath.toString().startsWith(app.packageId)
        }
    }

    private fun findUniqueResources(modules: List<AndroidModule>): List<ResourceId> {
        val uniqueIds = modules
            .flatMap { it.ids }
            .unique()

        return uniqueIds.map { id ->
            val module = requireNotNull(modules.find { module ->
                module.ids.contains(id)
            })
            ResourceId(id, module)
        }
    }

    private fun <E> Collection<E>.unique(): Collection<E> {
        val map = mutableMapOf<E, Int>()
        this.forEach { element ->
            val count = map[element]
            if (count == null) {
                map[element] = 1
            } else {
                map[element] = count + 1
            }
        }
        return map.filter { it.value == 1 }.keys
    }

    private fun createReplacementForResource(
        imports: KtImportList,
        resource: ResourceId
    ): ClassReplacementResolution {
        val rClassName = resource.module.packageId + ".R"
        val oldImport = imports.findImport(rClassName)
        val import = oldImport
            ?: elementsFactory.createImportDirective(rClassName, resource.module.packageId.substringAfterLast('.') + "_R")

        val element = elementsFactory.createExpression("${import.referencedClass}.id.${resource.id}")

        val newImport = if (oldImport == null) import else null
        return ClassReplacementResolution(element, newImport)
    }

    private class ClassReplacementResolution(val element: PsiElement, val importDirective: KtImportDirective?)
}

private val KtImportDirective.referencedClass: String
    get() = if (aliasName == null) {
        children.last().children[1].text
    } else {
        aliasName!!
    }
