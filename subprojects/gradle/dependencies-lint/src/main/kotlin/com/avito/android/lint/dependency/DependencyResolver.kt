package com.avito.android.lint.dependency

import com.avito.android.androidAppExtension
import com.avito.android.androidLibraryExtension
import com.avito.android.lint.util.JarContent
import com.avito.impact.configuration.internalModule
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.attributes.Attribute
import org.gradle.api.internal.artifacts.DefaultBuildIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.DefaultProjectComponentIdentifier
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier
import org.gradle.util.Path
import org.objectweb.asm.ClassReader
import java.io.File
import java.net.URLClassLoader

internal class DependencyResolver(private val project: Project) {

    private val logger = project.logger

    fun suspiciousDependencies(): Collection<SuspiciousDependency> {
        val declaredDependencies = declaredDependencies()
        val references = classReferences()
        val dependenciesUsages = findDependenciesUsages(references)
        val unusedDependencies: List<DependencyUsage> = dependenciesUsages
            .filter { declaredDependencies.contains(it.component.selected.id) }
            .filterNot { it is DependencyUsage.Direct }

        val resolutions: List<SuspiciousDependency> = unusedDependencies
            .map { componentUsage ->
                val referencedComponents = references.all
                    .map { it.artifact.id.componentIdentifier }
                    .toSet()

                val usedTransitiveComponents = componentUsage.component.dependencies()
                    .filter {
                        referencedComponents.contains(it.selected.id)
                    }
                    .map { it.selected.id }
                    .toSet()

                return@map if (usedTransitiveComponents.isEmpty()) {
                    SuspiciousDependency.Unused(componentUsage.component.selected.id)
                } else {
                    SuspiciousDependency.UsedTransitively(
                        componentUsage.component.selected.id,
                        usedTransitiveComponents
                    )
                }

            }

        return resolutions
    }

    private class DependencyReferences(
        val direct: Set<ArtifactUsage>,
        val indirect: Set<ArtifactUsage>
    ) {
        val all = direct + indirect
    }

    private fun classReferences(): DependencyReferences {
        val classpath = sourceSetClasspath()

        val outputDirectories = sourceSetOutput()
        val artifactsByClass = artifactsByClass()
        val compiledSourceClassLoader: ClassLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())

        val directReferences = mutableSetOf<ArtifactUsage>()
        val indirectReferences = mutableSetOf<ArtifactUsage>()

        // TODO: parallelize
        outputDirectories.forEach { output ->
            output.walk()
                .filter { it.isClass() }
                .onEach { classFile ->
                    val visitor = DependencyClassVisitor(artifactsByClass, compiledSourceClassLoader)
                    try {
                        classFile.inputStream().use { inputStream ->
                            ClassReader(inputStream).accept(visitor, ClassReader.SKIP_DEBUG)
                        }
                    } catch (t: Throwable) {
                        // https://github.com/nebula-plugins/gradle-lint-plugin/issues/88 - ArrayIndexOutOfBounds in ASM
                        logger.info("unable to read class $classFile $t") // TODO: log and solve problems
                    } finally {
                        directReferences.addAll(visitor.directReferences)
                        indirectReferences.addAll(visitor.indirectReferences)
                    }
                }
                .map { Unit }
                .toSet()
        }
        return DependencyReferences(
            directReferences,
            indirectReferences
        )
    }

    /**
     * Full classpath of module with all dependencies
     */
    private fun sourceSetClasspath(): Set<File> {
        return when {
            project.plugins.hasPlugin("com.android.library") -> androidLibraryClasspath()
            project.plugins.hasPlugin("com.android.application") -> androidAppClasspath()
            project.plugins.hasPlugin("kotlin") -> kotlinModuleClasspath()
            else -> throw IllegalStateException("Module ${project.path} has no supported plugins")
        }
    }

    private fun androidLibraryClasspath(): Set<File> {
        val ext = project.androidLibraryExtension
        return (ext.libraryVariants + ext.unitTestVariants + ext.testVariants)
            .flatMap { it.getCompileClasspath(null) }
            .toSet()
    }

    private fun androidAppClasspath(): Set<File> {
        val ext = project.androidAppExtension
        return (ext.applicationVariants + ext.unitTestVariants + ext.testVariants)
            .flatMap { it.getCompileClasspath(null) }
            .toSet()
    }

    private fun kotlinModuleClasspath(): Set<File> {
        val classesDir = File(project.buildDir, "classes")
        val languagesDirs = classesDir.listFiles().orEmpty()
        val sourceSetsDirs: List<File> = languagesDirs.flatMap {
            it.listFiles().orEmpty().toList()
        }
        return sourceSetsDirs.toSet()
    }

    private fun sourceSetOutput(): Set<File> {
        return project.internalModule.implementationConfiguration.bytecodeSets() +
                project.internalModule.testConfiguration.bytecodeSets() +
                project.internalModule.androidTestConfiguration.bytecodeSets()
    }

    // TODO: memoize
    private fun artifactsByClass(): Map<String, Set<ResolvedArtifactResult>> {
        val artifacts: Set<ResolvedArtifactResultWrapper> = project.configurations
            .filter { it.canBeConsumedDirectly() }
            .flatMap { artifactsFrom(it) }
            .map { ResolvedArtifactResultWrapper(it) }
            .toSet()

        val artifactsByClass = mutableMapOf<String, MutableSet<ResolvedArtifactResult>>()
        // TODO: parallelize
        artifacts.forEach { artifact ->
            artifact.files.forEach { file ->
                JarContent(file).classes.forEach { clazz ->
                    artifactsByClass.getOrPut(clazz, { mutableSetOf() }).add(artifact)
                }
            }
        }
        return artifactsByClass
    }

    private fun artifactsFrom(configuration: Configuration): Collection<ResolvedArtifactResult> {
        return try {
            val artifactType = Attribute.of("artifactType", String::class.java)

            arrayOf("jar", "aar").flatMap { type ->
                configuration.incoming
                    .artifactView {
                        it.attributes {
                            it.attribute(artifactType, type)
                        }
                    }
                    .artifacts.artifacts
            }
        } catch (ignore: Exception) {
            emptySet()
        }
    }

    // TODO: pass exceptions through config or filter on class level
    private fun declaredDependencies(): Set<ComponentIdentifier> {
        return project.configurations
            .filter { it.canBeConsumedDirectly() }
            .flatMap { it.dependencies }
            .map { it.asComponentIdentifier() }
            .filterNot {
                (it is UnknownComponent)
                        || (it is ModuleComponentIdentifier && it.group == "com.android.tools.build")
                        || (it is ModuleComponentIdentifier && it.group == "org.jetbrains.kotlin")
            }
            .toSet()
    }

    private fun Dependency.asComponentIdentifier(): ComponentIdentifier {
        return when (val dependency = this) {
            is ProjectDependency -> DefaultProjectComponentIdentifier(
                DefaultBuildIdentifier.ROOT,
                Path.path(dependency.dependencyProject.path),
                Path.path(dependency.dependencyProject.path),
                dependency.dependencyProject.name
            )
            is ExternalModuleDependency -> DefaultModuleComponentIdentifier(
                DefaultModuleIdentifier.newId(dependency.group.orEmpty(), dependency.name),
                dependency.version.orEmpty()
            )
            else -> UnknownComponent(dependency.toString())
        }
    }

    private fun findDependenciesUsages(references: DependencyReferences): Set<DependencyUsage> {
        val usedArtifacts: Set<ComponentIdentifier> = references.all
            .map { it.artifact.id.componentIdentifier }
            .toSet()

        return resolveDependencies(project).map { dependency ->
            val isUsedDirectly = usedArtifacts.contains(dependency.selected.id)
            if (isUsedDirectly) {
                DependencyUsage.Direct(dependency)
            } else {
                val usedDependencies = dependency.dependencies()
                    .filter { usedArtifacts.contains(it.selected.id) }
                    .toSet()
                if (usedDependencies.isEmpty()) {
                    DependencyUsage.Unused(dependency)
                } else {
                    DependencyUsage.Transitive(dependency, usedDependencies)
                }
            }
        }.toSet()
    }

    private fun resolveDependencies(project: Project): List<ResolvedDependencyResult> {
        return project.configurations
            .filter { it.canBeConsumedDirectly() }
            .flatMap { configuration ->
                try {
                    val rootDependencies = configuration.incoming.resolutionResult.root.dependencies
                    rootDependencies.mapNotNull { rootDependency: DependencyResult ->
                        if (rootDependency is ResolvedDependencyResult) {
                            rootDependency
                        } else {
                            null
                        }
                    }
                } catch (ignore: Throwable) {
                    emptyList<ResolvedDependencyResult>()
                }
            }
    }

    private fun Configuration.canBeConsumedDirectly(): Boolean {
        // TODO: runtime, compile
        return !(
                name.startsWith("lint")
                        || name.startsWith("kapt")
                )
    }

    // TODO memoize
    private fun ResolvedDependencyResult.dependencies(): Set<ResolvedDependencyResult> {
        val dependency = this
        val dependencies = mutableSetOf<ResolvedDependencyResult>()
        try {
            val rootDependencies = dependency.selected.dependencies
            traverseDependencies(isRootLevel = true, dependencies = rootDependencies) { _, result ->
                dependencies.add(result)
            }
        } catch (ignore: Throwable) {
        }
        return dependencies
    }

    private fun traverseDependencies(
        isRootLevel: Boolean,
        dependencies: Set<DependencyResult>,
        action: (isRootLevel: Boolean, result: ResolvedDependencyResult) -> Unit
    ) {
        for (result in dependencies) {
            when (result) {
                is ResolvedDependencyResult -> {
                    action(isRootLevel, result)
                    traverseDependencies(
                        isRootLevel = false,
                        dependencies = result.selected.dependencies,
                        action = action
                    )
                }
                else -> {
                    // ignore
                }
            }
        }
    }

    private fun File.isClass() = isFile && (extension == "class")

}

private sealed class DependencyUsage(val component: ResolvedDependencyResult) {

    class Direct(component: ResolvedDependencyResult) : DependencyUsage(component)

    class Unused(component: ResolvedDependencyResult) : DependencyUsage(component)

    class Transitive(
        component: ResolvedDependencyResult,
        val transitive: Collection<ResolvedDependencyResult>
    ) : DependencyUsage(component)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DependencyUsage) return false

        if (component.selected.id != other.component.selected.id) return false

        return true
    }

    override fun hashCode(): Int {
        return component.selected.id.hashCode()
    }

}
