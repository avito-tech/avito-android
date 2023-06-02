package com.avito.module_api_extraction

import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import okio.buffer
import okio.sink
import okio.source
import java.io.File

internal open class ModuleApiExtractor {
    fun extract(
        moduleNames: List<String>,
        inputJsonFiles: Set<File>,
        outputDir: File
    ) {
        val moduleToProjectsMap = readModuleToProjectsMap(moduleNames, inputJsonFiles)

        val moduleToDeclaredClassesMap = computeModuleToDeclaredClassesMap(moduleToProjectsMap)
        val moduleToUsedClassesMap = computeModuleToUsedClassesMap(moduleToProjectsMap)
        val classToUsingModuleMap = computeClassToUsingModuleMap(moduleToUsedClassesMap)

        val moduleToExposedClassesToUsingModules = computeModuleToExposedClassesToUsingModules(
            moduleToDeclaredClassesMap,
            classToUsingModuleMap
        )

        writeResultToFiles(moduleToExposedClassesToUsingModules, outputDir)
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

    private fun computeModuleToExposedClassesToUsingModules(
        moduleToDeclaredClassesMap: Map<ModuleEntry, Set<ClassEntry>>,
        classToUsingModuleMap: Map<ClassEntry, Set<ModuleEntry>>
    ): Map<ModuleEntry, Map<ClassEntry, Set<ModuleEntry>>> {
        return moduleToDeclaredClassesMap.mapValues { (module, declaredClasses) ->
            declaredClasses.asSequence().mapNotNull { declaredClass ->
                val usingModules = classToUsingModuleMap[declaredClass] ?: return@mapNotNull null
                if (usingModules.all { it == module }) return@mapNotNull null
                declaredClass to usingModules - module
            }.toMap()
        }
    }

    private fun writeResultToFiles(
        moduleToExposedClassesMap: Map<ModuleEntry, Map<ClassEntry, Set<ModuleEntry>>>,
        outputDir: File
    ) {
        moduleToExposedClassesMap.forEach { (module, classToUsingModulesMap) ->
            val outputFile = outputDir.resolve(module.name + ".json")
            writeResultToFile(classToUsingModulesMap, outputFile)
        }
    }

    private fun writeResultToFile(
        classToUsingModulesMap: Map<ClassEntry, Set<ModuleEntry>>,
        outputFile: File
    ) {
        val outputSink = outputFile.sink().buffer()

        JsonWriter.of(outputSink).use { writer ->
            writer.indent = "    "
            writer.beginObject()

            classToUsingModulesMap.forEach { (usedClass, classes) ->
                writer.name(usedClass.name)
                writer.beginArray()

                classes.forEach {
                    writer.value(it.name)
                }

                writer.endArray()
            }

            writer.endObject()
        }
    }

    data class ModuleEntry(val name: String)
    data class ClassEntry(val name: String)
}
