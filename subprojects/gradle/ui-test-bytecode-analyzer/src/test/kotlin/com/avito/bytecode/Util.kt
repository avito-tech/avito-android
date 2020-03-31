package com.avito.bytecode

import com.avito.bytecode.graph.OneDirectedGraph
import com.avito.bytecode.invokes.bytecode.context.Context
import com.avito.bytecode.invokes.bytecode.context.ContextLoader
import com.avito.bytecode.invokes.bytecode.find.TargetClassesFinderImpl
import com.avito.bytecode.invokes.bytecode.find.TestMethodsFinderImpl
import com.avito.bytecode.invokes.bytecode.tracer.InvokesTracerImpl
import com.avito.bytecode.metadata.IdFieldExtractor
import com.avito.bytecode.target.TargetClassesDetector
import org.apache.bcel.classfile.JavaClass
import java.io.File
import java.util.jar.JarFile

const val INTERFACE_DETECTION = "com.example.dimorinny.example.screen.ScreenInterface"
const val FIXTURE_PATH = "fixture.jar"

const val METADATA_FIELD_NAME = "id"

data class InvocationGraphBuildingResult(
    val context: Context,
    val invocationGraph: OneDirectedGraph<Node>,
    val targetClasses: Set<JavaClass>,
    val invocationsOnTargetClasses: Set<Node>
)

fun prepareInvocationsFinder(): InvocationGraphBuildingResult {
    val context = ContextLoader().load(getFixtureJarFile())

    val invocationsOnTargetClasses: MutableSet<Node> = mutableSetOf()
    val targetClasses = TargetClassesFinderImpl(
        targetClassesDetector = TargetClassesDetector.InterfaceBasedDetector(
            INTERFACE_DETECTION
        )
    ).find(context)

    val isExecutedOnTargetClassClass: (Node, Set<JavaClass>) -> Boolean =
        { node: Node, affectedClasses: Set<JavaClass> ->
            affectedClasses.find { it.className == node.className } != null
        }

    val invocationGraph = InvocationGraphBuilder(
        invokesTracer = InvokesTracerImpl()
    ).build(
        context = context
    ) { _: Node, to: Node ->
        if (isExecutedOnTargetClassClass(to, targetClasses)) invocationsOnTargetClasses.add(to)
    }

    return InvocationGraphBuildingResult(
        context = context,
        invocationGraph = invocationGraph,
        targetClasses = targetClasses,
        invocationsOnTargetClasses = invocationsOnTargetClasses
    )
}

fun prepareMetadataExtractor() = IdFieldExtractor.Impl(fieldName = METADATA_FIELD_NAME, logger = { println(it) })

fun findInvocations(): Map<String, Collection<String>> = prepareInvocationsFinder().let {
    TargetClassesByTestsInvokesFinder().find(
        invocationGraph = it.invocationGraph,
        invocationsOnTargetClasses = it.invocationsOnTargetClasses,
        tests = TestMethodsFinderImpl().find(it.context)
    )
}

fun extractMetadata(): Set<IdFieldExtractor.ScreenToId> = ContextLoader().load(getFixtureJarFile()).let {
    prepareMetadataExtractor().extract(
        context = it,
        targetClasses = TargetClassesFinderImpl(
            targetClassesDetector = TargetClassesDetector.InterfaceBasedDetector(
                interfaceName = INTERFACE_DETECTION
            )
        ).find(it)
    )
}

fun extractTargetClasses(): Set<String> = ContextLoader().load(getFixtureJarFile()).let {
    TargetClassesFinderImpl(
        targetClassesDetector = TargetClassesDetector.InterfaceBasedDetector(
            interfaceName = INTERFACE_DETECTION
        )
    ).find(it)
}
    .map { it.className }
    .toSet()

fun fileFromJarResources(name: String) = File("src/test/resources/$name")

fun getFixtureJarFile(): JarFile = JarFile(fileFromJarResources(FIXTURE_PATH))
