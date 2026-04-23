package ru.netology.nework.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("auth.preferences") }
    )

    private object Keys {
        val ID = longPreferencesKey("id")
        val TOKEN = stringPreferencesKey("token")
    }

    val authState: Flow<AuthState> = dataStore.data.map { prefs ->
        AuthState(
            id = prefs[Keys.ID] ?: 0L,
            token = prefs[Keys.TOKEN],
        )
    }

    suspend fun setAuth(id: Long, token: String) {
        dataStore.edit { prefs ->
            prefs[Keys.ID] = id
            prefs[Keys.TOKEN] = token
        }
    }

    suspend fun clearAuth() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.ID)
            prefs.remove(Keys.TOKEN)
        }
    }
}
