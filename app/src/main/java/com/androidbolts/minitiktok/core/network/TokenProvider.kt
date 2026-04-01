package com.androidbolts.minitiktok.core.network

interface TokenProvider {
   suspend fun getToken(): String?
}