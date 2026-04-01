package com.androidbolts.minitiktok.core.utils.stringprovider

interface StringProvider {
    fun getString(resId: Int): String
    fun getString(resId: Int, vararg formatArgs: Any): String
}