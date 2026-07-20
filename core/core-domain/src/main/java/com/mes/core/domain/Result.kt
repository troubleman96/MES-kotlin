package com.mes.core.domain

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure(val code: String, val message: String) : Result<Nothing>
    data object NetworkError : Result<Nothing>
    data object Loading : Result<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
    val isNetworkError: Boolean get() = this is NetworkError
    val isLoading: Boolean get() = this is Loading

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
        is NetworkError -> this
        is Loading -> this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun failure(code: String, message: String): Result<Nothing> = Failure(code, message)
        fun <T> networkError(): Result<T> = NetworkError
        fun <T> loading(): Result<T> = Loading
    }
}
