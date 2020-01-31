package com.avito.bytecode

import com.avito.bytecode.graph.OneDirectedGraph
import com.avito.bytecode.invokes.bytecode.model.FoundMethod

class TargetClassesByTestsInvokesFinder {

    fun find(
        invocationGraph: OneDirectedGraph<Node>,
        invocationsOnTargetClasses: Set<Node>,
        tests: Set<FoundMethod>? = null
    ): Map<String, Set<String>> {
        val mutableTests: MutableSet<FoundMethod>? = tests?.toMutableSet()

        return matchInvocationsFromTestMethodsToTarget(
            graph = invocationGraph,
            invocationsOnTargetClasses = invocationsOnTargetClasses,
            tests = mutableTests
        )
    }

    private fun matchInvocationsFromTestMethodsToTarget(
        graph: OneDirectedGraph<Node>,
        invocationsOnTargetClasses: Set<Node>,
        tests: MutableSet<FoundMethod>?
    ): MutableMap<String, MutableSet<String>> {

        val result: MutableMap<String, MutableSet<String>> = mutableMapOf()

        invocationsOnTargetClasses.forEach {
            val affectedTests = graph.findAllAccessibleNodes(from = it) { it.isTest == true }
                .map { it.name }
                .toMutableSet()

            // Remove tests, which used in current target class
            tests?.removeAll(
                tests.filter { affectedTests.contains(it.name) }
            )

            if (it.className in result) {
                result[it.className]!!.addAll(affectedTests)
            } else {
                result[it.className] = affectedTests
            }
        }

        tests?.apply {
            // Save tests, which didn't used in any target class
            result[DIRTY_STUB] = tests.map { it.name }.toMutableSet()
        }

        return result
    }
}
