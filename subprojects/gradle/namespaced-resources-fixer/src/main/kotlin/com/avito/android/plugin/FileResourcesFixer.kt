package com.avito.android.plugin

import io.github.detekt.parser.KtCompiler
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import java.io.File

/**
 * Replace unambiguous R class references to Application by libraries R classes
 */
internal class FileResourcesFixer {

    private val compiler = KtCompiler()

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

        val replacedModules = replaceReferences(references, uniqueResources)
        fixImports(ktFile, replacedModules)

        return ktFile.text
    }

    private fun replaceReferences(
        rClassReferences: List<PsiElement>,
        uniqueResources: List<ResourceId>
    ): Set<AndroidModule> {
        val replacedModules = mutableSetOf<AndroidModule>()
        rClassReferences.forEach { refElement ->
            val resource = uniqueResources.find { resource ->
                refElement.text.endsWith(".id.${resource.id}")
            }
            if (resource != null) {
                val newReference = createElementForResourceRef(resource)
                refElement.replace(newReference)
                replacedModules.add(resource.module)
            }
        }
        return replacedModules
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

        private val KtImportDirective.referencedClass: String
            get() = if (aliasName == null) {
                children.last().children[1].text
            } else {
                aliasName!!
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

    private fun fixImports(file: KtFile, modules: Set<AndroidModule>) {
        val newImports: List<KtImportDirective> = modules.map {
            createImportForModule(it)
        }
        val importList: KtImportList = requireNotNull(file.children.find { it is KtImportList }) as KtImportList
        newImports.forEach { newImport ->
            val hasNewImport: Boolean = importList.imports
                .any {
                    it.importedReference?.text == newImport.importedReference?.text
                }
            if (!hasNewImport) {
                importList.add(createNewLineElement())
                importList.add(newImport)
            }
        }
    }

    private fun createImportForModule(module: AndroidModule): KtImportDirective {
        val alias = importAliasForResource(module)

        val file = compiler.createKtFile("import ${module.packageId}.R as $alias")
        return file.children.find { it is KtImportList }!!.children[0] as KtImportDirective
    }

    private fun createNewLineElement(): PsiWhiteSpace {
        val file = compiler.createKtFile(
            """

            import R
            
            """.trimIndent()
        )
        return file.children.find { it is PsiWhiteSpace }!! as PsiWhiteSpace
    }

    private fun createElementForResourceRef(resource: ResourceId): PsiElement {
        val alias = importAliasForResource(resource.module)

        val file = compiler.createKtFile("val id = $alias.id.${resource.id}")
        return file.children.find { it is KtProperty }!!.children[0]
    }

    private fun importAliasForResource(module: AndroidModule): String =
        module.packageId.substringAfterLast('.') + "_R"

}
