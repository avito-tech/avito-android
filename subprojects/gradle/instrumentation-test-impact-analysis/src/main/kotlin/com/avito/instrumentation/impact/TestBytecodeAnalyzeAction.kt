package com.avito.instrumentation.impact

import com.avito.android.isAndroid
import com.avito.bytecode.InvocationGraphBuilder
import com.avito.bytecode.Node
import com.avito.bytecode.TargetClassesByTestsInvokesFinder
import com.avito.bytecode.graph.OneDirectedGraph
import com.avito.bytecode.invokes.bytecode.context.Context
import com.avito.bytecode.invokes.bytecode.context.ContextLoader
import com.avito.bytecode.invokes.bytecode.find.TargetClassesFinderImpl
import com.avito.bytecode.invokes.bytecode.find.TestMethodsFinderImpl
import com.avito.bytecode.invokes.bytecode.model.FoundMethod
import com.avito.bytecode.invokes.bytecode.tracer.InvokesTracerImpl
import com.avito.bytecode.report.JsonFileReporter
import com.avito.bytecode.target.TargetClassesDetector
import com.avito.impact.BytecodeResolver
import com.avito.impact.ModifiedProject
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.ReportType
import com.avito.impact.changes.ChangeType
import com.avito.impact.configuration.internalModule
import com.avito.impact.util.AndroidPackage
import com.avito.impact.util.AndroidProject
import com.avito.impact.util.Screen
import com.avito.impact.util.Test
import com.avito.instrumentation.impact.metadata.ModulePathExtractor
import com.avito.instrumentation.impact.metadata.ScreenToModulePath
import com.avito.instrumentation.impact.model.AffectedTest
import com.avito.instrumentation.impact.model.AffectionType
import com.avito.utils.logging.CILogger
import com.google.gson.GsonBuilder
import org.apache.bcel.classfile.JavaClass
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.internal.hash.Hasher
import org.gradle.internal.isolation.Isolatable
import org.gradle.internal.snapshot.ValueSnapshot
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jetbrains.kotlin.psi.KtClass
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@Suppress("UnstableApiUsage")
abstract class TestBytecodeAnalyzeAction : WorkAction<TestBytecodeAnalyzeAction.Params> {

    private val ciLogger: CILogger
        get() = parameters.state.ciLogger

    private val bytecodeResolver
        get() = parameters.state.bytecodeResolver

    private val config
        get() = parameters.state.config

    private val byteCodeAnalyzeSummary
        get() = parameters.state.byteCodeAnalyzeSummary

    private val project
        get() = parameters.state.project

    private val finder
        get() = parameters.state.finder

    private val gson = GsonBuilder().setPrettyPrinting().create()

    abstract class Params : WorkParameters {
        class State(
            val ciLogger: CILogger,
            val byteCodeAnalyzeSummary: Provider<RegularFile>,
            val bytecodeResolver: BytecodeResolver,
            val config: InstrumentationTestImpactAnalysisExtension,
            val project: Project,
            val finder: ModifiedProjectsFinder
        ) : Isolatable<State> {

            override fun <S : Any?> coerce(type: Class<S>): S? {
                return null
            }

            override fun isolate(): State {
                return this
            }

            override fun appendToHasher(hasher: Hasher) {
                throw UnsupportedOperationException()
            }

            override fun asSnapshot(): ValueSnapshot {
                throw UnsupportedOperationException()
            }
        }

        abstract var state: State
    }

    override fun execute() {
        val contextLoader = ContextLoader()
        val foldersWithClassesToLoad = bytecodeResolver.resolveBytecodeWithoutDependencyToAnotherConfigurations(
            reportType = ReportType.ANDROID_TESTS
        )
        val context: Context = contextLoader.load(foldersWithClassesToLoad)

        val affectedAndroidTestModules = getAffectedAndroidTestModules()

        val invocationGraphResult = getInvocationGraph(
            config = config,
            context = context,
            affectedAndroidTestModules = affectedAndroidTestModules
        )

        val bytecodeAnalyzeSummary = BytecodeAnalyzeSummary(
            testsByScreen = getTestsByScreen(
                context = context,
                invocationGraph = invocationGraphResult.invocationGraph,
                invocationsOnScreens = invocationGraphResult.invocationsOnTargetClasses
            ),
            testsAffectedByDependentOnUserChangedCode = getTestsAffectedByDependentOnUserChangedCode(
                invocationGraph = invocationGraphResult.invocationGraph,
                invocationsOnChangedClasses = invocationGraphResult.invocationsOnChangedClasses
            ),
            testsModifiedByUser = getTestsModifiedByUser(
                context = context,
                changedClasses = invocationGraphResult.changedClasses
            ),
            screenToModule = getScreenToModule(
                fieldName = config.screenMarkerMetadataField.get(),
                targetClasses = invocationGraphResult.targetClasses,
                affectedAndroidTestModules = affectedAndroidTestModules,
                screenClass = config.screenMarkerClass.get()
            )
        )

        JsonFileReporter(
            path = byteCodeAnalyzeSummary.get().asFile,
            gson = gson
        ).report(bytecodeAnalyzeSummary)
    }

    @Suppress("DEPRECATION")
    private fun getAffectedAndroidTestModules(): Map<AndroidPackage, ModifiedProject> =
        finder.findModifiedProjectsWithoutDependencyToAnotherConfigurations(reportType = ReportType.ANDROID_TESTS)
            .asSequence()
            .filter { it.project.isAndroid() }
            .map { AndroidProject(it.project).debug.manifest.getPackage() to it }
            .toMap()

    private fun getChangedClasses(
        context: Context,
        affectedAndroidTestModules: Map<AndroidPackage, ModifiedProject>
    ): Map<ChangeType, Collection<JavaClass>> {
        val targetModule = AndroidProject(project)
        val targetModulePackage = targetModule.debug.manifest.getPackage()

        val classesFinder = ModifiedKotlinClassesFinder(
            projectDir = project.rootDir,
            logger = ciLogger
        )

        return if (targetModulePackage in affectedAndroidTestModules) {
            val changedFiles = affectedAndroidTestModules[targetModulePackage]!!.changedFiles
            val patterns: Map<ChangeType, List<Regex>> = changedFiles
                .map { changedFile ->
                    classesFinder.find(changedFile.relativePath)
                        .map { classRegex -> changedFile.changeType to classRegex }
                }.flatten()
                .groupByTo(
                    destination = mutableMapOf(),
                    keySelector = { it.first },
                    valueTransform = { it.second }
                )

            ciLogger.info("Finding changed classes using patterns: $patterns...")
            val changedClasses = patterns.mapValues { (_, patterns) ->
                val changedClassesFinder = TargetClassesFinderImpl(
                    targetClassesDetector = TargetClassesDetector.RegexFileNameDetector(
                        regex = patterns
                    )
                )
                changedClassesFinder.find(context)
            }

            ciLogger.info("Found ${changedClasses.size} changed classes")
            return changedClasses
        } else {
            emptyMap()
        }
    }

    private fun getTargetClasses(
        config: InstrumentationTestImpactAnalysisExtension,
        context: Context
    ): Set<JavaClass> {
        val targetClassesFinder = TargetClassesFinderImpl(
            targetClassesDetector = TargetClassesDetector.InterfaceBasedDetector(
                config.screenMarkerClass.get()
            )
        )

        ciLogger.info("Finding ${config.screenMarkerClass} classes...")
        val targetClasses = targetClassesFinder.find(context)
        ciLogger.info("Found ${targetClasses.size} affected classes")

        return targetClasses
    }

    private fun getInvocationGraph(
        config: InstrumentationTestImpactAnalysisExtension,
        context: Context,
        affectedAndroidTestModules: Map<AndroidPackage, ModifiedProject>
    ): InvocationGraphCreationResult {
        val invocationGraphBuilder = InvocationGraphBuilder(
            invokesTracer = InvokesTracerImpl()
        )

        val invocationsOnChangedClasses: MutableSet<Node> = ConcurrentHashMap.newKeySet()
        val invocationsOnTargetClasses: MutableSet<Node> = ConcurrentHashMap.newKeySet()

        val changedClasses = getChangedClasses(context, affectedAndroidTestModules)

        val targetClasses = getTargetClasses(
            config = config,
            context = context
        )

        val isExecutedOnTargetClassClass: (Node, Set<JavaClass>) -> Boolean =
            { node: Node, affectedClasses: Set<JavaClass> ->
                affectedClasses.find { it.className == node.className } != null
            }

        ciLogger.info("Invocation tracing started")
        lateinit var invocationGraph: OneDirectedGraph<Node>
        val tracingTime = measureTimeMillis {
            val allChangedClasses = changedClasses.values.flatten().toSet()
            invocationGraph = invocationGraphBuilder.build(
                context = context,
                hook = { _: Node, to: Node ->
                    if (isExecutedOnTargetClassClass(to, allChangedClasses)) invocationsOnChangedClasses.add(to)
                    if (isExecutedOnTargetClassClass(to, targetClasses)) invocationsOnTargetClasses.add(to)
                }
            )
        }
        ciLogger.info("Invocation tracing completed in ${TimeUnit.MILLISECONDS.toSeconds(tracingTime)}s")

        return InvocationGraphCreationResult(
            changedClasses = changedClasses,
            targetClasses = targetClasses,
            invocationsOnChangedClasses = invocationsOnChangedClasses,
            invocationsOnTargetClasses = invocationsOnTargetClasses,
            invocationGraph = invocationGraph
        )
    }

    private fun getTestsAffectedByDependentOnUserChangedCode(
        invocationGraph: OneDirectedGraph<Node>,
        invocationsOnChangedClasses: Set<Node>
    ): Set<AffectedTest> {

        val invokesFinder = TargetClassesByTestsInvokesFinder()

        ciLogger.info("Detecting affections from changed classes to tests...")
        val invocations = invokesFinder.find(
            invocationGraph = invocationGraph,
            invocationsOnTargetClasses = invocationsOnChangedClasses
        )
        ciLogger.info("Completed detecting affections from screens to tests")

        return invocations.entries.map { (test, methods) ->
            AffectedTest(test, AffectionType.DEPENDENT_TEST_CODE_CHANGED, methods)
        }.toSet()
    }

    private fun getTestsModifiedByUser(
        context: Context,
        changedClasses: Map<ChangeType, Collection<JavaClass>>
    ): Set<AffectedTest> {
        ciLogger.info("Finding test methods inside changed classes...")
        val testMethodsFinder = TestMethodsFinderImpl()
        return changedClasses
            .map { (changeType, changedClasses) ->
                changedClasses.map { changeType to it }
            }
            .flatten()
            .asSequence()
            .map { (changeType, changedClass) ->
                val testMethods = testMethodsFinder.find(context, listOf(changedClass))
                AffectedTest(
                    changedClass.className,
                    changeType.toAffectReason(),
                    testMethods.map { it.name }.toSet()
                )
            }
            .filter { it.methods.isNotEmpty() }
            .toSet()
    }

    private fun getTestsByScreen(
        context: Context,
        invocationGraph: OneDirectedGraph<Node>,
        invocationsOnScreens: Set<Node>
    ): Map<Screen, Set<Test>> {
        val invokesFinder = TargetClassesByTestsInvokesFinder()
        val tests: Set<FoundMethod> = TestMethodsFinderImpl().find(context)
        return invokesFinder.find(
            invocationGraph = invocationGraph,
            invocationsOnTargetClasses = invocationsOnScreens,
            tests = tests
        )
    }

    private fun getScreenToModule(
        fieldName: String,
        screenClass: String,
        targetClasses: Set<JavaClass>,
        affectedAndroidTestModules: Map<AndroidPackage, ModifiedProject>
    ): Set<ScreenToModulePath> {
        ciLogger.info("affectedAndroidTestModule = ${affectedAndroidTestModules.map { it.key to it.value.project.path }}")
        affectedAndroidTestModules.mapValues { (_, project) ->
            getScreenFiles(project.project, screenClass)
        }
        val extractor = ModulePathExtractor.Impl(fieldName)
        return extractor.extract(targetClasses.map { it.fileName })
    }

    private fun getScreenFiles(project: Project, screenClass: String) {
        val ktc = KotlinCompiler(ModifiedKotlinClassesFinder.createKotlinCoreEnvironment())
        val sourceSets = project.internalModule.androidTestConfiguration.sourceSets()

        ciLogger.info("ANALYZIIIING")
        val startTime = System.currentTimeMillis()

        sourceSets.forEach { dir ->
            ciLogger.info("dir = ${dir.path}")

            dir.walkTopDown()
                .filter {
                    ciLogger.info("file = ${it.path}")
                    it.extension == "kt"
                }
                .map { ktc.compile(it) }
                .forEach { ktFile ->
                    ciLogger.info("ktFile = ${ktFile.name}")

                    ktFile.declarations
                        .filterIsInstance<KtClass>()
                        .forEach {
                            ciLogger.info("declaration = ${it.name}; implements = ${it.superTypeListEntries.map { it.typeAsUserType?.referenceExpression?.getIdentifier()?.text }}}")
                        }
                }
        }

        ciLogger.info("DONE ANALYZIIIING in ${System.currentTimeMillis() - startTime}ms")
    }

    data class InvocationGraphCreationResult(
        val changedClasses: Map<ChangeType, Collection<JavaClass>>,
        val targetClasses: Set<JavaClass>,
        val invocationsOnChangedClasses: Set<Node>,
        val invocationsOnTargetClasses: Set<Node>,
        val invocationGraph: OneDirectedGraph<Node>
    )
}

private fun ChangeType.toAffectReason(): AffectionType {
    return when (this) {
        ChangeType.ADDED -> AffectionType.TEST_ADDED
        ChangeType.COPIED -> AffectionType.TEST_COPIED
        ChangeType.DELETED -> AffectionType.TEST_DELETED
        ChangeType.MODIFIED -> AffectionType.TEST_MODIFIED
        ChangeType.RENAMED -> AffectionType.TEST_RENAMED
    }
}
