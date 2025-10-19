package com.dibachain.smfn.flags

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore single-instance
private val Context.authDataStore by preferencesDataStore("auth_prefs")

class AuthPrefs(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
    }

    // ✅ همیشه non-null: وقتی چیزی نباشه "" می‌ده
    val token: Flow<String> = context.authDataStore.data.map { it[KEY_TOKEN] ?: "" }

    suspend fun setToken(token: String) {
        context.authDataStore.edit { it[KEY_TOKEN] = token }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.remove(KEY_TOKEN) }
    }
}

