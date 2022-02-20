package ru.avito.image_builder.internal.docker

import java.time.Duration

internal interface Docker {

    fun build(
        vararg args: String,
        timeout: Duration = LONG_OPERATION_TIMEOUT
    ): Result<ImageId>

    fun push(
        vararg args: String,
        timeout: Duration = LONG_OPERATION_TIMEOUT
    ): Result<Unit>

    /**
     * @return output
     */
    fun run(
        vararg args: String,
        timeout: Duration = LONG_OPERATION_TIMEOUT
    ): Result<String>

    /**
     * @return output
     */
    fun exec(
        vararg args: String,
        timeout: Duration = LONG_OPERATION_TIMEOUT
    ): Result<String>

    fun login(
        username: String,
        password: String,
        registry: String? = null,
        timeout: Duration = FAST_OPERATION_TIMEOUT
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
        timeout: Duration = FAST_OPERATION_TIMEOUT
    ): Result<Unit>

    fun commit(
        /**
         * Apply Dockerfile instruction to the created image
         */
        change: String,
        container: String,
        timeout: Duration = LONG_OPERATION_TIMEOUT
    ): Result<ImageId>

    fun remove(
        vararg options: String,
        container: String,
        timeout: Duration = FAST_OPERATION_TIMEOUT
    ): Result<Unit>
}

private val FAST_OPERATION_TIMEOUT = Duration.ofSeconds(30)
private val LONG_OPERATION_TIMEOUT = Duration.ofMinutes(10)
