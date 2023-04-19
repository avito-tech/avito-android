package com.avito.logger

/**
 * Helps do not fail at configuration time when create logger via the GradleLoggerFactory
 */
internal class LazyLogger(factory: () -> Logger) : Logger {
    private val delegate by lazy { factory() }

    override fun verbose(msg: String) {
        delegate.verbose(msg)
    }

    override fun debug(msg: String) {
        delegate.debug(msg)
    }

    override fun info(msg: String) {
        delegate.info(msg)
    }

    override fun warn(msg: String, error: Throwable?) {
        delegate.warn(msg, error)
    }

    override fun critical(msg: String, error: Throwable) {
        delegate.critical(msg, error)
    }
}
