package com.tonicantarella.gymtracker.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey

object GymPreferences {
    val USER_HAS_BEEN_WELCOMED = booleanPreferencesKey("user_has_been_welcomed")
    val SHOW_UNSAVED_CHANGES_DIALOG = booleanPreferencesKey("show_unsaved_changes_dialog")
    val SHOW_FINISH_WORKOUT_CONFIRM_DIALOG =
        booleanPreferencesKey("show_finish_workout_confirm_dialog")
}