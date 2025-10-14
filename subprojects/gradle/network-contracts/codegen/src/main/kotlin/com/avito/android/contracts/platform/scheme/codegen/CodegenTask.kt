package com.avito.android.contracts.platform.scheme.codegen

import com.avito.android.Result
import com.avito.android.contracts.platform.output.OutputTransformer
import com.avito.android.contracts.platform.output.OutputType
import com.avito.android.contracts.platform.scheme.codegen.config.CodegenConfig
import com.avito.android.contracts.platform.scheme.codegen.executor.Codegen
import com.avito.android.contracts.platform.shared.throwGradleError
import com.avito.android.isFailure
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.utils.ProcessRunner
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import java.io.File
import java.time.Duration

@CacheableTask
public abstract class CodegenTask : DefaultTask() {

    @get:Input
    internal abstract val kind: Property<String>

    @get:Input
    internal abstract val codegenProjectName: Property<String>

    @get:Input
    internal abstract val packageName: Property<String>

    @get:Input
    @get:Optional
    internal abstract val apiClassName: Property<String>

    @get:Input
    internal abstract val moduleName: Property<String>

    @get:Input
    internal abstract val flags: SetProperty<String>

    @get:Input
    internal abstract val skipValidation: Property<Boolean>

    @get:Input
    @get:Optional
    internal abstract val crtEnvName: Property<String>

    @get:Input
    @get:Optional
    internal abstract val keyEnvName: Property<String>

    @get:Input
    internal abstract val timeoutSeconds: Property<Long>

    @get:Input
    internal abstract val generators: ListProperty<String>

    @get:Input
    internal abstract val mappings: MapProperty<String, String>

    @get:Input
    @get:Optional
    internal abstract val errorOutputType: Property<OutputType>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    internal abstract val tmpCrtFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    internal abstract val tmpKeyFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    internal abstract val codegenExecutableFiles: ConfigurableFileCollection

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val schemesDir: DirectoryProperty

    @get:OutputDirectory
    internal abstract val outputDirectory: DirectoryProperty

    @get:OutputFile
    internal abstract val codegenFile: RegularFileProperty

    @get:Internal
    internal abstract val moduleDirectory: DirectoryProperty

    @get:Internal
    internal abstract val loggerFactory: Property<LoggerFactory>

    @get:Internal
    internal abstract val errorOutputTransformer: Property<OutputTransformer>

    private val logger: Logger by lazy { loggerFactory.get().create("CodegenTask") }

    @TaskAction
    internal fun generate() {
        val arch = findOperatingSystemArchitecture(outputDirectory.get().asFile)
        check(arch !is Arch.Unknown) { "Unsupported OS system: ${arch.rawValue}" }

        val generators = generators.get()
        val config = CodegenConfig(
            packageName = packageName.get(),
            apiClassName = apiClassName.orNull.orEmpty(),
            schemesDirectoryRelativePath = schemesDir.get().asFile.toRelativeString(moduleDirectory.get().asFile),
            buildDirectoryRelativePath = outputDirectory.get().asFile.toRelativeString(moduleDirectory.get().asFile),
            moduleName = moduleName.get(),
            kind = kind.get(),
            name = codegenProjectName.get(),
            moduleDir = moduleDirectory.get().asFile,
            skipValidation = skipValidation.get(),
            crtEnv = crtEnvName.orNull to tmpCrtFile.orNull?.asFile?.toPath(),
            keyEnv = keyEnvName.orNull to tmpKeyFile.orNull?.asFile?.toPath(),
            flags = flags.get(),
            timeout = Duration.ofSeconds(timeoutSeconds.get()),
            errorOutputType = errorOutputType.orNull,
            mappings = mappings.get().orEmpty(),
            generators = generators,
        )
        val codegen = Codegen.create(arch, codegenExecutableFiles, logger, config)

        if (generators.size > 1) {
            val updateResult = codegen.update()
            if (updateResult.isFailure()) {
                throwCodegenException(updateResult)
            }
        }

        val result = codegen.execute(
            skipValidation = skipValidation.get()
        )

        if (result.isFailure()) {
            throwCodegenException(result)
        }
    }

    private fun findOperatingSystemArchitecture(processWorkingDirectory: File): Arch {
        val current = OperatingSystem.current()
        return when {
            current.isMacOsX -> {
                val processRunner = ProcessRunner.create(processWorkingDirectory)

                when (val archResult = processRunner.getOsxArchProcessor()) {
                    is Result.Success -> archResult.value
                    is Result.Failure -> throwGradleError(archResult.throwable.message.toString())
                }
            }

            current.isLinux -> Arch.LinuxAmd64

            else -> Arch.Unknown(current.name)
        }
    }

    private fun throwCodegenException(result: Result.Failure<String>): Nothing {
        val causeMessage = result.throwable.cause?.message.orEmpty()
        val message = errorOutputTransformer.orNull?.transform(causeMessage) ?: result.throwable.message
        throw GradleException("Network contracts generation failed:\n$message", result.throwable)
    }

    public companion object {
        public const val NAME: String = "codegen"
    }
}

private fun ProcessRunner.getOsxArchProcessor() =
    run("/usr/bin/uname -m")
        .map(String::trim)
        .map(Arch.Companion::getArch)
