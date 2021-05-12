package com.avito.graph

import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.DirectedWeightedMultigraph

/**
 * @operations nodes describing a DAG
 */
class ShortestPath<T : Operation>(private val operations: Set<T>) {

    private val operationByKey: Map<String, Operation> = operations.map { it.id to it }.toMap()

    fun find(): List<T> {
        if (operations.size <= 1) return operations.toList()

        val graph = build()
        val path = graph.shortestPath()

        @Suppress("UNCHECKED_CAST")
        return path.vertexList
            .filterNot { it.isSynthetic() } as List<T>
    }

    /***
     * Builds a graph to find the shortest path.
     * Let's see by example. We have nodes describing the graph:
     *
     * ```
     * A(1) <----┐
     *           │
     * B(1) <--- D(3) <--- E(1)
     *
     * F(2) <------------- G(2)
     * ```
     *
     * Root nodes: E, G
     * Leaf nodes: A, B, F
     *
     * This will be represented as:
     *
     * ```
     *   ┌-----(1)-- A <--(3)---┐
     *   ↓                      │
     * SINK <--(1)-- B <--(3)-- D <--(1)-- E <-- SOURCE
     *   ↑                                         │
     *   └-----(2)-- F <--(2)------------- G <-----┘
     * ```
     *
     * - A node weight become an edge weight
     * - Single source and sink are needed to find the shortest path
     *     without checking all combinations of roots and leaves
     * - Dependencies in graph are kinda inverted for faster computing in case we need to compare multiple paths.
     *   It's more efficient than recomputing paths for different sources.
     */
    private fun build(): Graph<Operation, DefaultWeightedEdge> {
        val graph: Graph<Operation, DefaultWeightedEdge> =
            DirectedWeightedMultigraph(DefaultWeightedEdge::class.java)

        addNodes(graph)
        addSource(graph)
        addSink(graph)

        checkGraphStructure(graph)

        return graph
    }

    private fun Graph<Operation, DefaultWeightedEdge>.shortestPath(): GraphPath<Operation, DefaultWeightedEdge> {
        // We could use negative weights to find the shortest path as solution for the longest path in DAG
        // Bellman–Ford algorithm supports negative weights but Dijkstra doesn't.
        // TODO: try topological sorting, it will be more efficient - O(V+E) instead of O(V*E)
        return BellmanFordShortestPath(this)
            .getPaths(source)
            .getPath(sink)
    }

    private fun checkGraphStructure(graph: Graph<Operation, DefaultWeightedEdge>) {
        checkSinkLinks(graph)
        checkSourceLinks(graph)
    }

    private fun checkSinkLinks(graph: Graph<Operation, DefaultWeightedEdge>) {
        graph.incomingEdgesOf(sink)
            .forEach { edgeToSink ->
                val leafNode = graph.getEdgeSource(edgeToSink)

                check(!leafNode.isSynthetic() && leafNode.isLeaf()) {
                    "Expected structure: sink <-- leaf node <--- node ..."
                }
            }
    }

    private fun checkSourceLinks(graph: Graph<Operation, DefaultWeightedEdge>) {
        graph.outgoingEdgesOf(source).forEach { edgeToRootNode ->
            val rootNode = graph.getEdgeTarget(edgeToRootNode)

            check(isRoot(rootNode) && !rootNode.isSynthetic()) {
                "Expected structure: ... node <-- root node <-- source"
            }
        }
    }

    private fun isRoot(operation: Operation): Boolean {
        return operations
            .firstOrNull { it.predecessors.contains(operation.id) } == null
    }

    private fun addNodes(graph: Graph<Operation, DefaultWeightedEdge>) {
        operations.forEach { node ->
            graph.addVertex(node)
        }
        operations.forEach { node ->
            node.findDependencies().forEach { dependency ->
                graph.addWeightedEdge(node, dependency)
            }
        }
    }

    private fun addSource(graph: Graph<Operation, DefaultWeightedEdge>) {
        graph.addVertex(source)

        rootNodes()
            .forEach { root ->
                graph.addWeightedEdge(source, root)
            }
    }

    private fun addSink(graph: Graph<Operation, DefaultWeightedEdge>) {
        graph.addVertex(sink)

        operations
            .filter { it.isLeaf() }
            .forEach { leafNode ->
                graph.addWeightedEdge(leafNode, sink)
            }
    }

    private fun Graph<Operation, DefaultWeightedEdge>.addWeightedEdge(
        from: Operation,
        to: Operation
    ) {
        addEdge(from, to)
        setEdgeWeight(from, to, from.duration)
    }

    private fun rootNodes(): Set<Operation> {
        val roots = mutableSetOf<Operation>()
        operations.forEach { roots.add(it) }

        operations.forEach { node ->
            node.findDependencies().forEach { dependency ->
                roots.remove(dependency)
            }
        }
        return roots
    }

    private fun Operation.findDependencies(): List<Operation> {
        // Skip missing nodes
        return predecessors.mapNotNull { dependencyKey ->
            operationByKey[dependencyKey]
        }
    }

    /**
     * Synthetically added operation
     */
    private fun Operation.isSynthetic(): Boolean =
        this == source || this == sink

    private val source = SimpleOperation("source")
    private val sink = SimpleOperation("sink")
}
