package com.avito.android

import com.avito.composite_exception.CompositeException

/**
 * Why not [kotlin.Result]?
 *  - It's usage as return type is experimental
 *    [KEEP](https://github.com/Kotlin/KEEP/blob/master/proposals/stdlib/result.md#limitations)
 *  - it requires to enable special compiler option to go without error
 *  - it sometimes exposed in modules API, which will require clients to enable this compiler options
 *
 * Could be replace with [kotlin.Result] as it's return type usage matures to stable
 */
public sealed class Result<T> {

    public fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw throwable
    }

    public fun getOrElse(func: (Throwable) -> T): T = when (this) {
        is Success -> value
        is Failure -> func(throwable)
    }

    public inline fun <R> map(
        mapSuccess: (value: T) -> R
    ): Result<R> = when (this) {
        is Success -> try {
            Success(mapSuccess(value))
        } catch (throwable: Throwable) {
            Failure(throwable)
        }
        is Failure -> Failure(throwable)
    }

    public inline fun <R> flatMap(
        mapSuccess: (value: T) -> Result<R>
    ): Result<R> = when (this) {
        is Success -> try {
            mapSuccess(value)
        } catch (e: Throwable) {
            Failure(e)
        }
        is Failure -> Failure(throwable)
    }

    public inline fun <R> fold(onSuccess: (value: T) -> R, onFailure: (throwable: Throwable) -> R): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(throwable)
    }

    public inline fun recover(func: (Throwable) -> T): Result<T> = when (this) {
        is Success -> this
        is Failure -> try {
            Success(func(throwable))
        } catch (t: Throwable) {
            Failure(t)
        }
    }

    public inline fun rescue(f: (Throwable) -> Result<T>): Result<T> = when (this) {
        is Success -> this
        is Failure -> try {
            f(throwable)
        } catch (t: Throwable) {
            Failure(t)
        }
    }

    public inline fun exists(predicate: (T) -> Boolean): Boolean = when (this) {
        is Success -> try {
            predicate(getOrThrow())
        } catch (e: Throwable) {
            false
        }
        is Failure -> false
    }

    public inline fun onSuccess(func: (T) -> Unit): Result<T> = when (this) {
        is Success -> {
            func(value)
            this
        }
        is Failure -> this
    }

    public inline fun onFailure(func: (Throwable) -> Unit): Result<T> = when (this) {
        is Success -> this
        is Failure -> {
            func(throwable)
            this
        }
    }

    public inline fun <R> combine(other: Result<T>, func: (T, T) -> R): Result<R> {
        return when {
            this is Success && other is Success -> Success(func(this.value, other.value))
            this is Failure && other is Success -> Failure(this.throwable)
            this is Success && other is Failure -> Failure(other.throwable)
            this is Failure && other is Failure -> Failure(
                CompositeException(
                    "${this.throwable.message}\n${other.throwable.message}",
                    arrayOf(this.throwable, other.throwable)
                )
            )
            else -> throw IllegalStateException("this shouldn't happen")
        }
    }

    public fun isSuccess(): Boolean = this is Success

    public fun isFailure(): Boolean = this is Failure

    public class Success<T>(public val value: T) : Result<T>() {

        override fun equals(other: Any?): Boolean = when (other) {
            is Success<*> -> value == other.value
            else -> false
        }

        override fun hashCode(): Int = value?.hashCode() ?: 0

        override fun toString(): String = "Success[$value]"
    }

    public class Failure<T>(public val throwable: Throwable) : Result<T>() {

        override fun equals(other: Any?): Boolean = when (other) {
            is Failure<*> -> throwable == other.throwable
            else -> false
        }

        override fun hashCode(): Int = throwable.hashCode()

        override fun toString(): String = "Failure[${throwable.message}]"
    }

    public companion object {

        public inline fun <T> tryCatch(func: () -> T): Result<T> = try {
            Success(func.invoke())
        } catch (e: Throwable) {
            Failure(e)
        }
    }
}
