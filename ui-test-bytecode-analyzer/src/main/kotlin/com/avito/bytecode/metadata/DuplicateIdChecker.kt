package com.avito.bytecode.metadata

import com.avito.bytecode.metadata.IdFieldExtractor.ScreenToId

interface DuplicateIdChecker {

    fun check(screenToId: Set<ScreenToId>): Result

    class Impl(private val unknownId: Int) : DuplicateIdChecker {

        override fun check(screenToId: Set<ScreenToId>): Result =
            Result(screenToId
                .groupBy(
                    keySelector = { it.rootViewRId },
                    valueTransform = { it.screenClass })
                .filter { it.key != unknownId.toString() && it.value.size > 1 }
                .map { DuplicateId(it.key, it.value) }
                .toSet()
            )
    }

    data class DuplicateId(val rootViewId: String, val screenClasses: List<String>)

    data class Result(val ids: Set<DuplicateId>) {

        val hasDuplicates: Boolean = ids.isNotEmpty()
    }
}