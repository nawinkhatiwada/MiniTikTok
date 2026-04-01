package com.androidbolts.minitiktok.core.storage.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Constants used for shared preference.
 */
object PrefKeys {
    const val PREFS_NAME = "bizvue_master_prefs"
    val IS_FIRST_LOGIN = booleanPreferencesKey("isFirstLogin")
    val ACCESS_TOKEN = stringPreferencesKey("accessToken")

    val SELECTED_OFFICE = stringPreferencesKey("selectedOffice")
     val APP_CONFIGURATION_KEY = stringPreferencesKey("app_config")

    val SELECTED_DEPARTMENT = stringPreferencesKey("selectedDepartment")

}