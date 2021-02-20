package com.avito.android.build_checks.internal.unique_app_res

import com.android.ide.common.symbols.SymbolTable
import com.android.resources.ResourceType
import com.avito.android.build_checks.internal.unique_app_res.DuplicateResourcesFinder.ResourceDuplicate

internal interface DuplicateResourcesFinder {

    fun findDuplicates(): List<ResourceDuplicate>

    class ResourceDuplicate(
        val resource: Resource,
        val packages: List<String>
    )
}

internal class DuplicateResourcesFinderImpl(
    private val symbolTables: List<SymbolTable>,
    private val ignoredResourceTypes: Set<ResourceType>,
    private val ignoredResources: Set<Resource>
) : DuplicateResourcesFinder {

    override fun findDuplicates(): List<ResourceDuplicate> {
        val duplicatedSymbols = findDuplicatedSymbols()

        val duplicatedResources = duplicatedSymbols
            .flatMap { (resourceType, names) ->
                names.map { name ->
                    val resource = Resource(resourceType, name)
                    val packages = findPackageNames(resource)
                    ResourceDuplicate(resource, packages)
                }
            }
        return duplicatedResources
            .filterNot { duplicate ->
                ignoredResources.contains(duplicate.resource)
            }
    }

    private fun findDuplicatedSymbols(): Map<ResourceType, Set<String>> {
        val mergedSymbols = mutableMapOf<ResourceType, MutableSet<String>>()
        val duplicates = mutableMapOf<ResourceType, MutableSet<String>>()

        symbolTables.forEach { table ->
            table.resourceTypes
                .filterNot { ignoredResourceTypes.contains(it) }
                .forEach { resourceType ->
                    val names = mergedSymbols.getOrPut(resourceType) { mutableSetOf() }

                    table.getSymbolByResourceType(resourceType)
                        .map { it.name }
                        .forEach { symbolName ->
                            if (names.contains(symbolName)) {
                                duplicates.getOrPut(resourceType, { mutableSetOf() }).add(symbolName)
                            } else {
                                names.add(symbolName)
                            }
                        }
                }
        }
        return duplicates
    }

    private fun findPackageNames(resource: Resource): List<String> {
        return symbolTables
            .filter { symbolTable ->
                symbolTable.getSymbolByResourceType(resource.type)
                    .map { it.name }
                    .contains(resource.name)
            }
            .map { it.tablePackage }
    }
}
