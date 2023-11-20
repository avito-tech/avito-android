package ru.avito.image_builder

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import ru.avito.image_builder.internal.cli.BuildImage
import ru.avito.image_builder.internal.cli.PublishEmceeImage
import ru.avito.image_builder.internal.cli.PublishEmceeWorker
import ru.avito.image_builder.internal.cli.PublishEmulator
import ru.avito.image_builder.internal.cli.PublishImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

public object Main {

    @OptIn(ExperimentalCli::class)
    @JvmStatic
    public fun main(args: Array<String>) {
        Logger.getLogger("").apply {
            level = Level.INFO
            for (handler in handlers) {
                level = Level.INFO
                // Sets up logging format similar to Emcee queue
                handler.formatter = object : SimpleFormatter() {

                    // 2022-11-28 16:05:30.500
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS", Locale.ROOT)

                    override fun format(record: LogRecord): String {
                        // [INFO] 2022-11-25 16:05:30.500: Message
                        return "[${record.level}] ${dateFormatter.format(Date(record.millis))}: ${record.message}\n"
                    }
                }
            }
        }
        val parser = ArgParser(
            programName = "image-builder",
        )

        parser.subcommands(
            BuildImage("build", "Build image"),
            PublishImage("publish", "Build and publish image"),
            PublishEmulator("publishEmulator", "Build and publish Android emulator image"),
            PublishEmceeImage("publishEmceeImage", "Build and publish Emcee queue image"),
            PublishEmceeWorker("publishEmceeWorker", "Build and publish Emcee worker image")
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
