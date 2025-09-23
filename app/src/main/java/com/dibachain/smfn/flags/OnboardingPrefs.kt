package com.dibachain.smfn.flags


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

class OnboardingPrefs(private val context: Context) {
    private val KEY_SHOWN = booleanPreferencesKey("onboarding_shown")

    val shownFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[KEY_SHOWN] ?: false }

    suspend fun setShown() {
        context.dataStore.edit { it[KEY_SHOWN] = true }
    }
}
