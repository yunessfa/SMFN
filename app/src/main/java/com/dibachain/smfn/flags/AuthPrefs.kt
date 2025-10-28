    package com.dibachain.smfn.flags

    import android.content.Context
    import androidx.datastore.dataStore
    import androidx.datastore.preferences.core.booleanPreferencesKey
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
            private val KEY_USER_ID = stringPreferencesKey("user_id")
            private val KEY_USER_iSPREMIUM = booleanPreferencesKey("user_ispremium")
            val USER_AVATAR = stringPreferencesKey("user_avatar")

        }

        val token: Flow<String> = context.authDataStore.data.map { it[KEY_TOKEN] ?: "" }
        val userId: Flow<String> = context.authDataStore.data.map { it[KEY_USER_ID] ?: "" }
        val userAvatar: Flow<String> = context.authDataStore.data.map { it[USER_AVATAR].orEmpty() }

        val userIspremium: Flow<Boolean> = context.authDataStore.data.map { it[KEY_USER_iSPREMIUM] ?: false }
        suspend fun setUserId(value: String) {
            context.authDataStore.edit { it[KEY_USER_ID] = value }
        }
        suspend fun setUserAvatarLink(link: String?) = context.authDataStore.edit {
            it[USER_AVATAR] = link ?: ""
        }
        suspend fun setToken(token: String) {
            context.authDataStore.edit { it[KEY_TOKEN] = token }
        }
        suspend fun setisPremium(token: Boolean) {
            context.authDataStore.edit { it[KEY_USER_iSPREMIUM] = token }
        }

        suspend fun clear() {
            context.authDataStore.edit { it.remove(KEY_TOKEN) }
        }
    }

