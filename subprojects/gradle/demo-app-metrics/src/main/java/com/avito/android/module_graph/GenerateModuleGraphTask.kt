package com.avito.android.module_graph

import com.avito.android.module_graph.extractor.ModuleGraphInfoExtractor
import com.avito.android.module_graph.extractor.ModuleGraphInfoExtractorService
import com.avito.android.module_graph.models.GradleDependency
import com.avito.android.module_graph.models.ModuleGraphInfo
import com.avito.android.module_type.ModuleType
import kotlinx.serialization.encodeToString
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
public abstract class GenerateModuleGraphTask @Inject constructor(
    objects: ObjectFactory,
) : DefaultTask(), ModuleGraphTask {

    // Contains an object of type ModuleGraphInfo
    @get:OutputFile
    public abstract override val outputFile: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val projectDir: DirectoryProperty

    @get:Input
    public val dependencies: ListProperty<GradleDependency> =
        objects.listProperty(GradleDependency::class.java)

    @get:Input
    public val modulesToModuleTypes: MapProperty<String, ModuleType> =
        objects.mapProperty(String::class.java, ModuleType::class.java)

    @get:Internal
    internal abstract val infoExtractorService: Property<ModuleGraphInfoExtractorService>

    @TaskAction
    public fun traverse() {
        val moduleGraphInfoExtractor = ModuleGraphInfoExtractor(
            dependencies = dependencies.get(),
            modulesToModuleTypes = modulesToModuleTypes.get(),
            linesOfCodeCounter = infoExtractorService.get().linesOfCodeCounter,
            projectDir = projectDir.get().asFile,
        )
        val moduleGraphInfo = moduleGraphInfoExtractor.extractInfo()
        val encodedInfo = infoExtractorService.get().defaultJson.encodeToString<ModuleGraphInfo>(moduleGraphInfo)
        outputFile.get().asFile.writeText(encodedInfo)
    }

    public companion object {
        public const val NAME: String = "generateModuleGraph"
    }
}
