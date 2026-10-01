package com.ubeyd.focusguard

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.focusGuardDataStore by preferencesDataStore(
    name = "focus_guard_preferences"
)

class PreferencesManager(
    private val context: Context
) {

    private object Keys {

        val BLOCK_INSTAGRAM_REELS =
            booleanPreferencesKey(
                "block_instagram_reels"
            )

        val BLOCK_YOUTUBE_SHORTS =
            booleanPreferencesKey(
                "block_youtube_shorts"
            )
    }

    val blockInstagramReels: Flow<Boolean> =
        context.focusGuardDataStore.data.map { preferences ->
            preferences[
                Keys.BLOCK_INSTAGRAM_REELS
            ] ?: true
        }

    val blockYouTubeShorts: Flow<Boolean> =
        context.focusGuardDataStore.data.map { preferences ->
            preferences[
                Keys.BLOCK_YOUTUBE_SHORTS
            ] ?: true
        }

    suspend fun setBlockInstagramReels(
        enabled: Boolean
    ) {
        context.focusGuardDataStore.edit { preferences ->
            preferences[
                Keys.BLOCK_INSTAGRAM_REELS
            ] = enabled
        }
    }

    suspend fun setBlockYouTubeShorts(
        enabled: Boolean
    ) {
        context.focusGuardDataStore.edit { preferences ->
            preferences[
                Keys.BLOCK_YOUTUBE_SHORTS
            ] = enabled
        }
    }
}
