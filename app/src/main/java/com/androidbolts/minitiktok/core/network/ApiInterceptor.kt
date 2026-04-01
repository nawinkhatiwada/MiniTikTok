package com.androidbolts.minitiktok.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class ApiInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    companion object {
        private const val CODE_400 = 400
        private const val CODE_401 = 401
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        val isPublicApi = path.contains("login") || path.contains("register")
        val requestBuilder = originalRequest.newBuilder()
        requestBuilder.addHeader("Content-Type", "application/json")
        if (!isPublicApi) {
            val token = runBlocking { tokenProvider.getToken() }
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        val response = chain.proceed(requestBuilder.build())
        val responseBody = response.body
        val responseString = responseBody.string()

        // TODO: Handle specific HTTP codes
        when (response.code) {
            CODE_400 -> {
                // handle400Error(responseString, response)
            }
            CODE_401 -> {
                // throw UnAuthorizedException("Unauthorized Access!")
            }
        }
        val contentType = responseBody.contentType()
        return response.newBuilder()
            .body(responseString.toResponseBody(contentType))
            .build()
    }
}