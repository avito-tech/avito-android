package ru.avito.image_builder.internal.docker

import java.time.Duration

interface Docker {

    /**
     * @return image ID
     */
    fun build(
        vararg args: String,
        timeout: Duration = DEFAULT_LONG_TIMEOUT
    ): Result<String>

    fun push(
        vararg args: String,
        timeout: Duration = DEFAULT_LONG_TIMEOUT
    ): Result<Unit>

    /**
     * @return container ID
     */
    fun run(
        vararg args: String,
        timeout: Duration = DEFAULT_LONG_TIMEOUT
    ): Result<String>

    /**
     * @return output
     */
    fun exec(
        vararg args: String,
        timeout: Duration = DEFAULT_LONG_TIMEOUT
    ): Result<String>

    fun login(
        username: String,
        password: String,
        registry: String? = null,
        timeout: Duration = Duration.ofMinutes(1)
    ): Result<Unit>

    fun tag(
        /**
         * SOURCE_IMAGE[:TAG]
         */
        source: String,
        /**
         * TARGET_IMAGE[:TAG]
         */
        target: String,
        timeout: Duration = Duration.ofMinutes(1)
    ): Result<Unit>
}

private val DEFAULT_LONG_TIMEOUT = Duration.ofMinutes(10)
