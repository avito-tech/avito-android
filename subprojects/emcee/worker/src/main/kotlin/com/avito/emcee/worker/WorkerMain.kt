package com.avito.emcee.worker

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlin.time.ExperimentalTime

public object WorkerMain {

    @ExperimentalCli
    @ExperimentalTime
    @JvmStatic
    public fun main(args: Array<String>) {
        val parser = ArgParser(
            programName = "emcee-worker",
        )

        parser.subcommands(
            StartWorkerCommand(
                name = "start",
                description = "Start the worker. After start worker will try to get test bucket and execute it."
            )
        )
        parser.parse(args)
    }
}
