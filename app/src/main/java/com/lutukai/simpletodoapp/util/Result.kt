package com.lutukai.simpletodoapp.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()

    data class Failure(val exception: Throwable, val message: String = exception.message ?: "Unknown error") :
        Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (Throwable, String) -> Unit): Result<T> {
        if (this is Failure) action(exception, message)
        return this
    }
}

suspend fun <T> safeCall(dispatcher: CoroutineDispatcher, block: suspend () -> T): Result<T> = withContext(dispatcher) {
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Failure(e)
    }
}
