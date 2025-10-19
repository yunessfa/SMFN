package com.dibachain.smfn.flags

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore("profile_progress")

class ProfileProgressStore(private val context: Context) {
    companion object {
        private val STEP               = intPreferencesKey("step")
        private val PHONE              = stringPreferencesKey("phone")
        private val FULL_NAME          = stringPreferencesKey("full_name")
        private val USERNAME           = stringPreferencesKey("username")
        private val GENDER             = stringPreferencesKey("gender")
        private val AVATAR_URI         = stringPreferencesKey("avatar_uri")
        private val KYC_VIDEO_URI      = stringPreferencesKey("kyc_video_uri")
        private val INTERESTS_CSV      = stringPreferencesKey("interests_csv")
    }

    suspend fun saveStep(step: Int) {
        context.profileDataStore.edit { it[STEP] = step }
    }
    suspend fun savePhone(v: String) {
        context.profileDataStore.edit { it[PHONE] = v }
    }
    suspend fun savePersonal(full: String, user: String, gender: String) {
        context.profileDataStore.edit {
            it[FULL_NAME] = full; it[USERNAME] = user; it[GENDER] = gender
        }
    }
    suspend fun saveAvatar(uri: String?) {
        context.profileDataStore.edit { if (uri == null) it.remove(AVATAR_URI) else it[AVATAR_URI] = uri }
    }
    suspend fun saveKyc(uri: String?) {
        context.profileDataStore.edit { if (uri == null) it.remove(KYC_VIDEO_URI) else it[KYC_VIDEO_URI] = uri }
    }
    suspend fun saveInterests(keys: List<String>) {
        context.profileDataStore.edit { it[INTERESTS_CSV] = keys.joinToString(",") }
    }

    suspend fun load(): SavedProfile =
        context.profileDataStore.data.map { p ->
            SavedProfile(
                step = p[STEP] ?: 0,
                phone = p[PHONE].orEmpty(),
                fullName = p[FULL_NAME].orEmpty(),
                username = p[USERNAME].orEmpty(),
                gender = p[GENDER].orEmpty(),
                avatarUri = p[AVATAR_URI],
                kycVideoUri = p[KYC_VIDEO_URI],
                interests = p[INTERESTS_CSV]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            )
        }.first()

    suspend fun clear() { context.profileDataStore.edit { it.clear() } }
}

data class SavedProfile(
    val step: Int,
    val phone: String,
    val fullName: String,
    val username: String,
    val gender: String,
    val avatarUri: String?,
    val kycVideoUri: String?,
    val interests: List<String>
)
