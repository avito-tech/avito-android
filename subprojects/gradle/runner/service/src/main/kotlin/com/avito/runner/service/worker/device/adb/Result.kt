package com.avito.runner.service.worker.device.adb

import org.funktionale.tries.Try

sealed class Result<T> {

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw throwable
    }

    fun <R> map(transform: (value: T) -> R): Result<R> = when (this) {
        is Success -> try {
            Success(transform(value))
        } catch (throwable: Throwable) {
            Failure(throwable)
        }
        is Failure -> Failure(throwable)
    }

    fun <R> fold(successTransform: (value: T) -> R, failureTransform: (throwable: Throwable) -> R): R = when (this) {
        is Success -> successTransform(value)
        is Failure -> failureTransform(throwable)
    }

    fun recover(transform: (Throwable) -> T): Result<T> = when (this) {
        is Success -> this
        is Failure -> try {
            Success(transform(throwable))
        } catch (t: Throwable) {
            Failure(t)
        }
    }

    data class Success<T>(val value: T) : Result<T>()

    data class Failure<T>(val throwable: Throwable) : Result<T>()
}

/**
 * todo remove with funktionale
 */
fun <T> Result<T>.toTry(): Try<T> = when (this) {
    is Result.Success -> Try.Success(value)
    is Result.Failure -> Try.Failure(throwable)
}
