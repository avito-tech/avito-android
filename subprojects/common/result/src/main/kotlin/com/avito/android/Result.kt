package com.avito.android

import org.funktionale.tries.Try

sealed class Result<T> {

    abstract operator fun component1(): T?

    abstract operator fun component2(): Throwable?

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw throwable
    }

    fun getOrElse(transform: () -> T): T = when (this) {
        is Success -> value
        is Failure -> transform()
    }

    fun <R> map(transform: (value: T) -> R): Result<R> = when (this) {
        is Success -> try {
            Success(transform(value))
        } catch (throwable: Throwable) {
            Failure(throwable)
        }
        is Failure -> Failure(throwable)
    }

    fun <R> flatMap(transform: (value: T) -> Result<R>): Result<R> = when (this) {
        is Success -> try {
            transform(getOrThrow())
        } catch (e: Throwable) {
            Failure(e)
        }
        is Failure -> Failure(throwable)
    }

    fun <R> fold(onSuccess: (value: T) -> R, onFailure: (throwable: Throwable) -> R): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(throwable)
    }

    fun recover(transform: (Throwable) -> T): Result<T> = when (this) {
        is Success -> this
        is Failure -> try {
            Success(transform(throwable))
        } catch (t: Throwable) {
            Failure(t)
        }
    }

    class Success<T>(val value: T) : Result<T>() {

        override fun component1(): T? = value

        override fun component2(): Throwable? = null

        override fun equals(other: Any?): Boolean = when (other) {
            is Success<*> -> value == other.value
            else -> false
        }

        override fun hashCode(): Int = value?.hashCode() ?: 0

        override fun toString(): String = "Success[$value]"
    }

    class Failure<T>(val throwable: Throwable) : Result<T>() {

        override fun component1(): T? = null

        override fun component2(): Throwable? = throwable

        override fun equals(other: Any?): Boolean = when (other) {
            is Failure<*> -> throwable == other.throwable
            else -> false
        }

        override fun hashCode(): Int = throwable.hashCode()

        override fun toString(): String = "Failure[${throwable.message}]"
    }

    companion object {

        fun <T> tryCatch(body: () -> T): Result<T> = try {
            Success(body.invoke())
        } catch (e: Throwable) {
            Failure(e)
        }
    }
}

/**
 * todo remove with funktionale
 */
fun <T> Result<T>.toTry(): Try<T> = when (this) {
    is Result.Success -> Try.Success(value)
    is Result.Failure -> Try.Failure(throwable)
}
