package com.mes.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mes.core.domain.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mes_session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val accessToken: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }
    val refreshToken: Flow<String?> = dataStore.data.map { it[REFRESH_TOKEN_KEY] }
    val userId: Flow<String?> = dataStore.data.map { it[USER_ID_KEY] }
    val userRole: Flow<UserRole?> = dataStore.data.map { prefs ->
        prefs[USER_ROLE_KEY]?.let { UserRole.valueOf(it) }
    }
    val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { it[ONBOARDING_KEY] ?: false }
    val preferredLanguage: Flow<String> = dataStore.data.map { it[LANGUAGE_KEY] ?: "en" }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        role: UserRole
    ) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[USER_ID_KEY] = userId
            prefs[USER_ROLE_KEY] = role.name
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_ROLE_KEY)
        }
    }

    suspend fun setOnboardingComplete(role: UserRole) {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_KEY] = true
            prefs[USER_ROLE_KEY] = role.name
        }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language
        }
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val ONBOARDING_KEY = booleanPreferencesKey("has_seen_onboarding")
        private val LANGUAGE_KEY = stringPreferencesKey("preferred_language")
    }
}
