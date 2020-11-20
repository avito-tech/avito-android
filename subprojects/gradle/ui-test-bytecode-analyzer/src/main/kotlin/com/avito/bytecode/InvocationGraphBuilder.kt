package com.avito.bytecode

import com.avito.bytecode.graph.OneDirectedGraph
import com.avito.bytecode.invokes.bytecode.context.Context
import com.avito.bytecode.invokes.bytecode.fullName
import com.avito.bytecode.invokes.bytecode.tracer.InvokesTracer

typealias InvocationAddHook = (from: Node, to: Node) -> Unit

class InvocationGraphBuilder(
    private val invokesTracer: InvokesTracer
) {
    fun build(context: Context, hook: InvocationAddHook): OneDirectedGraph<Node> {
        val invocationGraph: OneDirectedGraph<Node> = OneDirectedGraph()

        invokesTracer.trace(context) { invoke ->
            val fromNode = Node(
                className = invoke.from.clazz.className,
                methodName = invoke.from.method.name,
                methodNameWithArguments = invoke.from.method.fullName,
                isTest = invoke.from.isTest
            )
            val toNode = Node(
                className = invoke.to.className,
                methodName = invoke.to.methodName,
                methodNameWithArguments = invoke.to.methodNameWithArguments,
                isTest = null
            )

            hook(fromNode, toNode)

            invocationGraph.putEdge(toNode, fromNode)
        }

        return invocationGraph
    }
}
