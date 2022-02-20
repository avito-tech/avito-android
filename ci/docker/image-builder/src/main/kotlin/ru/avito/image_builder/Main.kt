package ru.avito.image_builder

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import ru.avito.image_builder.internal.cli.BuildImage
import ru.avito.image_builder.internal.cli.PublishEmulator
import ru.avito.image_builder.internal.cli.PublishImage

public object Main {

    @OptIn(ExperimentalCli::class)
    @JvmStatic
    public fun main(args: Array<String>) {
        val parser = ArgParser(
            programName = "image-builder",
        )

        parser.subcommands(
            BuildImage("build", "Build image"),
            PublishImage("publish", "Build and publish image"),
            PublishEmulator("publishEmulator", "Build and publish Android emulator image")
        )
        parser.parse(sanitizeEmptyArgs(args))
    }

    private fun sanitizeEmptyArgs(args: Array<String>): Array<String> {
        return if (args.isEmpty()) {
            arrayOf("--help")
        } else {
            args
        }
    }
}
