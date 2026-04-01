package com.androidbolts.minitiktok.core.utils.stringprovider

import android.content.Context
import javax.inject.Inject

class StringProviderImpl @Inject constructor(private val context: Context) : StringProvider {

    override fun getString(resId: Int): String {
        return context.getString(resId)
    }

    override fun getString(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}