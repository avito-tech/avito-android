package com.avito.instrumentation.impact.metadata

import com.avito.instrumentation.impact.KotlinCompiler
import com.avito.instrumentation.impact.ModifiedKotlinClassesFinder
import com.avito.utils.logging.CILogger
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames
import java.io.File

typealias Screen = String
typealias PackageName = String

class MetadataParser(
    private val ciLogger: CILogger,
    screenClass: String,
    private val fieldName: String
) {

    private val kotlinCompiler: KotlinCompiler =
        KotlinCompiler(ModifiedKotlinClassesFinder.createKotlinCoreEnvironment())

    private val screenClassSimpleName = screenClass.substringAfterLast('.')

    fun parseMetadata(sourceSets: Collection<File>): Map<Screen, PackageName> {
        val startTime = System.currentTimeMillis()

        val result = mutableMapOf<Screen, PackageName>()

        sourceSets.forEach { dir ->
            dir.walkTopDown()
                .filter { it.extension == "kt" }
                .map { kotlinCompiler.compile(it) }
                .forEach { ktFile ->
                    ktFile.declarations
                        .filterIsInstance<KtClass>()
                        .filter {
                            it.getSuperNames().contains(screenClassSimpleName)
                        } //todo and import is correct, or fullname
                        .forEach {
                            val packageName = ktFile.packageFqName.asString()
                            val className = it.name!!
                            val fullClassName = if (packageName.isBlank()) {
                                className
                            } else {
                                "$packageName.$className"
                            }

                            it.body?.properties?.forEach { property ->
                                if (property.name == fieldName) {

                                    val expression = property.children.filterIsInstance<KtExpression>().single()

                                    val importLines = ktFile.importDirectives.map { it.importedFqName.toString() }
                                    result[fullClassName] = getPackageName(expression.text, importLines)!!
                                }
                            }
                        }
                }
        }

        ciLogger.info("Analyzed screen classes in ${System.currentTimeMillis() - startTime}ms")

        return result
    }
}

internal fun getPackageName(expression: String, importLines: List<String>): String? {
    return if (expression.startsWith("R")) {
        val import = importLines.find { it.endsWith(".R") }
        import?.substringBefore(".R")
    } else {
        expression.substringBefore(".R")
    }
}
