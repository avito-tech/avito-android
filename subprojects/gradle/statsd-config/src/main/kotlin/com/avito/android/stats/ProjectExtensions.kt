package com.avito.android.stats

import Slf4jGradleLoggerFactory
import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

// TODO extract to plugin
public val Project.statsd: Provider<StatsDSender> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    val property = project.objects.property(StatsDSender::class.java)
    // TODO delete when rewrite tests related to output
    val loggerFactory = if (project.hasProperty("statsd.test")) {
        testLoggerFactory()
    } else {
        Slf4jGradleLoggerFactory
    }
    val statsDSender = StatsDSender.create(
        config = config(project),
        loggerFactory = loggerFactory
    )
    property.set(statsDSender)
    property.finalizeValueOnRead()
    property
}

private fun testLoggerFactory() = object : LoggerFactory {
    override fun create(tag: String): Logger {
        return object : Logger {
            override fun debug(msg: String) {
                println("[$tag] $msg")
            }

            override fun info(msg: String) {
                println("[$tag] $msg")
            }

            override fun warn(msg: String, error: Throwable?) {
                println("[$tag] $msg")
            }

            override fun critical(msg: String, error: Throwable) {
                println("[$tag] $msg")
            }
        }
    }
}

public val Project.statsdConfig: Provider<StatsDConfig> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    Providers.of(config(project))
}

// todo need fail on configuration phase
private fun config(project: Project): StatsDConfig {
    val isEnabled = project.getBooleanProperty("avito.stats.enabled", false)
    return if (isEnabled) {
        StatsDConfig.Enabled(
            host = project.getMandatoryStringProperty("avito.stats.host"),
            fallbackHost = project.getMandatoryStringProperty("avito.stats.fallbackHost"),
            port = project.getMandatoryIntProperty("avito.stats.port"),
            namespace = SeriesName.create(
                project.getMandatoryStringProperty("avito.stats.namespace"),
                multipart = true
            )
        )
    } else {
        StatsDConfig.Disabled
    }
}
