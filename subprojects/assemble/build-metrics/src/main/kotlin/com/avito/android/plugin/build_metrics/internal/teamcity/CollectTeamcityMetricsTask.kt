package com.avito.android.plugin.build_metrics.internal.teamcity

import com.avito.android.graphite.GraphiteConfig
import com.avito.android.graphite.GraphiteSender
import com.avito.gradle.worker.inMemoryWork
import com.avito.teamcity.TeamcityApi
import com.avito.teamcity.TeamcityCredentials
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

// TODO: convert to CLI app.
//  It's no use to use Gradle for such tasks. They need to know nothing about a project.
internal abstract class CollectTeamcityMetricsTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @get:Input
    abstract val buildId: Property<String>

    @get:Input
    abstract val graphiteConfig: Property<GraphiteConfig>

    @get:Input
    abstract val teamcityCredentials: Property<TeamcityCredentials>

    @TaskAction
    fun action() {
        val buildId: String? = buildId.orNull
        require(!buildId.isNullOrBlank()) {
            "teamcity buildId property must be set"
        }
        val graphite = graphiteSender()
        val teamcity = TeamcityApi.create(teamcityCredentials.get())
        val action = CollectTeamcityMetricsAction(
            buildId = buildId,
            teamcityApi = teamcity,
            graphite = graphite
        )

        workerExecutor.inMemoryWork {
            action.execute()
        }
    }

    private fun graphiteSender(): GraphiteSender {
        return GraphiteSender.create(
            config = graphiteConfig.get()
        )
    }
}
