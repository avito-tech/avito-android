package com.avito.buildontarget

import com.avito.utils.logging.CILogger
import org.funktionale.tries.Try
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.events.OperationType
import org.gradle.tooling.events.ProgressListener
import java.io.File

class ConnectorNestedGradleRunner(private val logger: CILogger) : NestedGradleRunner {

    override fun run(
        workingDirectory: File,
        tasks: List<String>,
        buildScan: Boolean,
        jvmArgs: String,
        workers: Int,
        projectParams: Map<String, String>
    ): Try<Unit> {

        logger.info("Nested gradle runner run: $projectParams")

        val connection = GradleConnector.newConnector()
            .forProjectDirectory(workingDirectory)
            .connect()

        return Try {
            connection.use {

                @Suppress("UnstableApiUsage")
                it.newBuild()
                    .forTasks(*tasks.toTypedArray())
                    .addJvmArguments(jvmArgs)
                    .addArguments("-Dorg.gradle.workers.max=$workers")
                    .addArguments(projectParams.map { (key, value) -> "-P$key=$value" })
                    .addProgressListener(
                        ProgressListener { event -> logger.info(event.displayName) },
                        OperationType.TASK
                    )
                    .run()
            }
        }
    }
}
