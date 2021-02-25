package com.avito.android.build_checks.internal.unique_app_res

import com.android.ide.common.symbols.SymbolTable
import com.android.resources.ResourceType

internal interface UndeclaredResourcesFinder {

    fun findResources(): Set<Resource>
}

internal class UndeclaredResourcesFinderImpl(
    private val symbolTables: List<SymbolTable>,
    private val resources: Set<Resource>
) : UndeclaredResourcesFinder {

    override fun findResources(): Set<Resource> {
        val resourceTypes: Set<ResourceType> = resources.mapTo(mutableSetOf()) { it.type }
        val undeclared: MutableSet<Resource> = resources.toMutableSet()

        symbolTables.forEach { table ->
            table.resourceTypes
                .filter { resourceTypes.contains(it) }
                .forEach { resourceType ->
                    table.getSymbolByResourceType(resourceType)
                        .map { it.name }
                        .forEach { symbolName ->
                            undeclared.remove(Resource(resourceType, symbolName))
                        }
                }
        }
        return undeclared
    }
}
