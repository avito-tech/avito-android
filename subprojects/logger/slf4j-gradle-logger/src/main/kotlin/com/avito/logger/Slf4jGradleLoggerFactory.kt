import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import org.gradle.api.Task
import org.gradle.api.logging.Logging
import org.gradle.internal.logging.slf4j.DefaultContextAwareTaskLogger

public object Slf4jGradleLoggerFactory : LoggerFactory {
    override fun create(tag: String): Logger {
        return Slf4jGradleLogger(
            DefaultContextAwareTaskLogger(Logging.getLogger(Task::class.java)),
            tag
        )
    }
}

private class Slf4jGradleLogger(
    private val delegate: org.slf4j.Logger,
    private val tag: String
) : Logger {
    override fun debug(msg: String) {
        delegate.debug("[$tag] $msg")
    }

    override fun info(msg: String) {
        delegate.info("[$tag] $msg")
    }

    override fun warn(msg: String, error: Throwable?) {
        delegate.warn("[$tag] $msg", error)
    }

    override fun critical(msg: String, error: Throwable) {
        delegate.error("[$tag] $msg", error)
    }
}
