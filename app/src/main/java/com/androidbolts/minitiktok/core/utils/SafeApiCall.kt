package com.androidbolts.minitiktok.core.utils

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ResultType<T> {
    return try {
        ResultType.Success(apiCall())
    } catch (e: Exception) {
        ResultType.Error(e.message ?: "Something went wrong")
    }
}