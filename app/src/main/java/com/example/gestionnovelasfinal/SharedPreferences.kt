package com.example.gestionnovelasfinal

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class SharedPreferences {
    private val db: FirebaseFirestore = Firebase.firestore

    // Existing methods...

    suspend fun saveUserThemePreference(userId: String, isDarkTheme: Boolean) {
        val userPreferences = mapOf("isDarkTheme" to isDarkTheme)
        db.collection("userPreferences").document(userId).set(userPreferences).await()
    }

    suspend fun getUserThemePreference(userId: String): Boolean {
        val document = db.collection("userPreferences").document(userId).get().await()
        return document.getBoolean("isDarkTheme") ?: false
    }
    fun saveUserFavoriteNovels(userId: String, favoriteNovels: List<String>) {
        val json = gson.toJson(favoriteNovels)
        sharedPreferences.edit().putString("favorite_novels_$userId", json).apply()
    }

    fun getUserFavoriteNovels(userId: String): List<String> {
        val json = sharedPreferences.getString("favorite_novels_$userId", null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}