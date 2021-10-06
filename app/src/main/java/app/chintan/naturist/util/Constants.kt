package app.chintan.naturist.util

import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    const val USER_COLLECTION = "users"
    const val POST_COLLECTION = "posts"
    val ROLE_KEY = stringPreferencesKey("ROLE")
}