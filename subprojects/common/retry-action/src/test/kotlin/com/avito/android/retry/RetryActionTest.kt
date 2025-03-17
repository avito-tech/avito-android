package com.avito.android.retry

import com.avito.android.Result
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant

class RetryActionTest {

    @Test
    fun `test success on first attempt`() {
        var onSuccessCalled = false

        val result = executeWithRetries(
            maxAttempts = 3,
            delay = Duration.ZERO,
            onSuccess = { attempt, _, duration ->
                onSuccessCalled = true
                assertThat(attempt).isEqualTo(1)
                assertThat(duration.toNanos()).isGreaterThan(0)
            }
        ) {
            "first try success"
        }

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(onSuccessCalled).isTrue()
        result as Result.Success
        assertThat(result.value).isEqualTo("first try success")
    }

    @Test
    fun `test success after an initial failure`() {
        var attemptCounter = 0
        var failedTryCount = 0
        var onSuccessCalled = false

        val result = executeWithRetries(
            maxAttempts = 3,
            delay = Duration.ZERO,
            onFailedTry = { attempt, _, duration ->
                failedTryCount++
                assertThat(attempt).isEqualTo(1)
                assertThat(duration.toNanos()).isGreaterThan(0)
            },
            onSuccess = { attempt, _, duration ->
                onSuccessCalled = true
                assertThat(attempt).isEqualTo(2)
                assertThat(duration.toNanos()).isAtLeast(0)
            }
        ) {
            attemptCounter++
            @Suppress("KotlinConstantConditions")
            if (attemptCounter < 2) {
                error("failure on attempt $attemptCounter")
            } else {
                "success on attempt $attemptCounter"
            }
        }

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(onSuccessCalled).isTrue()
        result as Result.Success
        assertThat(result.value).isEqualTo("success on attempt 2")
        assertThat(failedTryCount).isEqualTo(1)
    }

    @Test
    fun `test failure after max attempts`() {
        var failedTryCount = 0
        var onFailureCalled = false
        val maxAttempts = 3

        val result = executeWithRetries(
            maxAttempts = maxAttempts,
            delay = Duration.ZERO,
            onFailedTry = { _, _, duration ->
                failedTryCount++
                assertThat(duration.toNanos()).isGreaterThan(0)
            },
            onFailure = { _, _, duration ->
                onFailureCalled = true
                assertThat(duration.toNanos()).isGreaterThan(0)
            }
        ) {
            throw IllegalStateException("always fails")
        }

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        result as Result.Failure
        assertThat(result.throwable.message).isEqualTo("always fails")
        assertThat(failedTryCount).isEqualTo(maxAttempts - 1)
        assertThat(onFailureCalled).isTrue()
    }

    @Test
    fun `test no retry when shouldRetry returns false`() {
        var onFailureCalled = false

        val result = executeWithRetries(
            maxAttempts = 3,
            delay = Duration.ZERO,
            shouldRetry = { _, _ -> Result.tryCatch { false } },
            onFailure = { _, _, _ ->
                onFailureCalled = true
            }
        ) {
            error("do not retry")
        }

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        result as Result.Failure
        assertThat(result.throwable.message).isEqualTo("do not retry")

        assertThat(onFailureCalled).isTrue()
    }

    @Test
    fun `test duration measurement on callbacks`() {
        var successDuration: Duration? = null
        var failureDuration: Duration? = null

        val resultSuccess = executeWithRetries(
            maxAttempts = 1,
            delay = Duration.ZERO,
            onSuccess = { _, _, duration ->
                successDuration = duration
            }
        ) {
            "ok"
        }
        assertThat(resultSuccess).isInstanceOf(Result.Success::class.java)
        assertThat(successDuration).isNotNull()
        assertThat(successDuration!!.toNanos()).isGreaterThan(0)

        val resultFailure = executeWithRetries(
            maxAttempts = 1,
            delay = Duration.ZERO,
            onFailure = { _, _, duration ->
                failureDuration = duration
            }
        ) {
            error("failure")
        }
        assertThat(resultFailure).isInstanceOf(Result.Failure::class.java)
        assertThat(failureDuration).isNotNull()
        assertThat(failureDuration!!.toNanos()).isGreaterThan(0)
    }

    @Test
    fun `test delay between attempts is respected`() {
        val delay = Duration.ofMillis(50)
        var attemptCounter = 0

        val start = Instant.now()
        val result = executeWithRetries(
            maxAttempts = 3,
            delay = delay,
            onFailedTry = { _, _, _ -> },
            onSuccess = { _, _, _ -> }
        ) {
            attemptCounter++
            @Suppress("KotlinConstantConditions")
            if (attemptCounter < 3) {
                error("failure on attempt $attemptCounter")
            } else {
                "final success"
            }
        }
        val end = Instant.now()
        val totalDuration = Duration.between(start, end)

        assertThat(totalDuration.toNanos()).isAtLeast(delay.toNanos() * 2)
        assertThat(result).isInstanceOf(Result.Success::class.java)
    }

    @Test
    fun `test error thrown if no attempt is made due to maxAttempts zero`() {
        val exception = assertThrows<IllegalStateException> {
            executeWithRetries(maxAttempts = 0, delay = Duration.ZERO) {
                "irrelevant"
            }
        }
        assertThat(exception.message).isEqualTo("retry must return value or throw exception")
    }
}
