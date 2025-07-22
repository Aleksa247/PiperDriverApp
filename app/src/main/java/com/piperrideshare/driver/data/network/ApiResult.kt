package com.piperrideshare.driver.data.network

sealed class ApiResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ApiResult<T>()

    data class Failure(
        val message: String,
        val code: Int? = null,
    ) : ApiResult<Nothing>()

    object NetworkError : ApiResult<Nothing>()
}
