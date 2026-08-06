package ru.avito.image_builder.internal.cli

import kotlinx.cli.ArgType
import kotlinx.cli.delimiter
import kotlinx.cli.required
import ru.avito.image_builder.internal.command.ApiLevel
import ru.avito.image_builder.internal.command.EmceeWorkerBuilder
import ru.avito.image_builder.internal.command.EmulatorType
import ru.avito.image_builder.internal.command.ImageTagger
import ru.avito.image_builder.internal.command.emulator.EmulatorPreparer
import ru.avito.image_builder.internal.command.emulator.EmulatorTester
import ru.avito.image_builder.internal.docker.CliDocker
import java.io.File

internal class PublishEmceeWorker(
    name: String,
    description: String,
) : BaseEmceeBuildImage(name, description) {

    private val apis: List<ApiLevel> by option(
        type = ApiLevelArg,
        description = "Space separated list of API versions, e.g. '27 35 37.0'"
    ).required()
        .delimiter(" ")

    private val emulatorLocale: String by option(
        type = ArgType.String,
        description = "Emulator locale in BCP 47 format. en-US locale is default."
    ).required()

    private val types: List<EmulatorType> by option(
        type = ArgType.Choice<EmulatorType>(),
        description = "Space separated list of types, e.g. 'google_apis google_atd'"
    ).required()
        .delimiter(" ")

    override fun execute() {
        val docker = CliDocker()

        check(apis.size == types.size) {
            "The number of APIs and types must be the same. APIs: $apis, Types: $types"
        }
        val apisAndTypes = apis.zip(types).toMap()

        val builder = EmceeWorkerBuilder(
            docker = docker,
            dockerfilePath = dockerfilePath,
            buildDir = File(buildDir),
            registry = registry,
            imageRegistryTagName = imageRegistryTagName,
            imageName = imageName,
            artifactoryUrl = artifactoryUrl,
            tagger = ImageTagger(docker, imageVersionTag),
            emulatorPreparer = EmulatorPreparer(docker, EmulatorTester(docker)),
            emulatorLocale = emulatorLocale,
            apisAndTypes = apisAndTypes,
        )
        buildOrPublishImage(docker, builder)
    }
}

private object ApiLevelArg : ArgType<ApiLevel>(hasParameter = true) {

    override val description: kotlin.String
        get() = "{ Api level, e.g. 30 or 37.0 }"

    override fun convert(value: kotlin.String, name: kotlin.String): ApiLevel = ApiLevel.parse(value)
}
