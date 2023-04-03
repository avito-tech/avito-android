package com.avito.module_api_extraction

import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import okio.Buffer
import okio.buffer
import okio.source
import java.io.File

internal open class ModuleApiExtractor {
    fun extract(
        moduleNames: List<String>,
        jsonFiles: Set<File>
    ): String {
        val moduleToProjectsMap = readModuleToProjectsMap(moduleNames, jsonFiles)

        val moduleToDeclaredClassesMap = computeModuleToDeclaredClassesMap(moduleToProjectsMap)
        val moduleToUsedClassesMap = computeModuleToUsedClassesMap(moduleToProjectsMap)
        val classToUsingModuleMap = computeClassToUsingModuleMap(moduleToUsedClassesMap)

        val moduleToExposedClassesMap = computeModuleToExposedClassesMap(
            moduleToDeclaredClassesMap,
            classToUsingModuleMap
        )

        return generateOutputJson(moduleToExposedClassesMap)
    }

    private fun readModuleToProjectsMap(
        moduleNames: List<String>,
        jsonFiles: Set<File>
    ): Map<ModuleEntry, List<SyntheticProject>> {
        val modules = moduleNames.asSequence().map { ModuleEntry(it) }
        val moduleToJsonFilePairs = modules.zip(jsonFiles.asSequence())

        val moshi = Moshi.Builder().build()
        val moshiAdapter = moshi.adapter(SyntheticProject::class.java)

        return moduleToJsonFilePairs
            .filter { it.second.exists() }
            .map { it.first to moshiAdapter.fromJson(it.second.source().buffer())!! }
            .groupBy({ it.first }, { it.second })
    }

    private fun computeModuleToDeclaredClassesMap(
        moduleToProjectsMap: Map<ModuleEntry, List<SyntheticProject>>
    ): Map<ModuleEntry, Set<ClassEntry>> {
        return moduleToProjectsMap.mapValues { (_, projects) ->
            projects.asSequence()
                .flatMap {
                    it.sources
                }
                .filterSourceClasses()
                .map { source ->
                    ClassEntry(source.className)
                }
                .toSet()
        }
    }

    private fun computeModuleToUsedClassesMap(
        moduleToProjectsMap: Map<ModuleEntry, List<SyntheticProject>>
    ): Map<ModuleEntry, Set<ClassEntry>> {
        return moduleToProjectsMap.mapValues { (_, projects) ->
            projects.asSequence()
                .flatMap {
                    it.sources
                }
                .filterSourceClasses()
                .flatMap { source ->
                    source.usedClasses.map { usedClass ->
                        ClassEntry(usedClass)
                    }
                }
                .toSet()
        }
    }

    private fun Sequence<SyntheticProject.Source>.filterSourceClasses() = filter { source ->
        source.type == "code"
            && !source.relativePath.startsWith("tmp/")
            && !source.relativePath.startsWith("intermediates/")
    }

    private fun computeClassToUsingModuleMap(
        moduleToUsedClassesMap: Map<ModuleEntry, Set<ClassEntry>>
    ): Map<ClassEntry, Set<ModuleEntry>> {
        val result = mutableMapOf<ClassEntry, MutableSet<ModuleEntry>>()
        for ((module, usedClasses) in moduleToUsedClassesMap) {
            for (usedClass in usedClasses) {
                val modules = result.getOrPut(usedClass, ::mutableSetOf)
                modules.add(module)
            }
        }
        return result
    }

    private fun computeModuleToExposedClassesMap(
        moduleToDeclaredClassesMap: Map<ModuleEntry, Set<ClassEntry>>,
        classToUsingModuleMap: Map<ClassEntry, Set<ModuleEntry>>
    ): Map<ModuleEntry, List<ClassEntry>> {
        return moduleToDeclaredClassesMap.mapValues { (module, declaredClasses) ->
            declaredClasses.filter { declaredClass ->
                val usingModules = classToUsingModuleMap[declaredClass] ?: emptySet()
                usingModules.any { it != module }
            }
        }
    }

    private fun generateOutputJson(moduleToExposedClassesMap: Map<ModuleEntry, List<ClassEntry>>): String {
        val output = Buffer()

        JsonWriter.of(output).use { writer ->
            writer.indent = "  "
            writer.beginObject()

            moduleToExposedClassesMap.forEach { (module, classes) ->
                writer.name(module.name)
                writer.beginArray()

                classes.forEach {
                    writer.value(it.name)
                }

                writer.endArray()
            }

            writer.endObject()
        }

        return output.readUtf8()
    }

    data class ModuleEntry(val name: String)
    data class ClassEntry(val name: String)
}
