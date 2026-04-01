package com.androidbolts.minitiktok.core.utils

sealed class ResultType<out T> {
    data class Success<T>(val data: T): ResultType<T>()
    data class Error(val message: String): ResultType<Nothing>()
}