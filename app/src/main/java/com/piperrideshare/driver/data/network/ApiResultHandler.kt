package com.piperrideshare.driver.data.network

import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(apiCall())
    } catch (e: HttpException) {
        val message = e.response()?.errorBody()?.string() ?: "HTTP ${e.code()} error"
        ApiResult.Failure(message, e.code())
    } catch (e: IOException) {
        ApiResult.NetworkError
    } catch (e: Exception) {
        ApiResult.Failure(e.message ?: "Unexpected error")
    }
