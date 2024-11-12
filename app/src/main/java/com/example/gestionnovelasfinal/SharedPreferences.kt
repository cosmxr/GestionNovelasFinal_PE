package com.example.gestionnovelasfinal

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class SharedPreferences {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun saveUserThemePreference(userId: String, isDarkTheme: Boolean) {
        val userPreferences = mapOf("isDarkTheme" to isDarkTheme)
        db.collection("userPreferences")
            .document(userId)
            .set(userPreferences, SetOptions.merge()) // Use merge to avoid overwriting other fields
            .await()
    }

    suspend fun getUserThemePreference(userId: String): Boolean {
        val document = db.collection("userPreferences").document(userId).get().await()
        return document.getBoolean("isDarkTheme") ?: false
    }

    suspend fun saveUserFavoriteNovels(userId: String, favoriteNovels: List<String>) {
        val userPreferences = mapOf("favoriteNovels" to favoriteNovels)
        db.collection("userPreferences")
            .document(userId)
            .set(userPreferences, SetOptions.merge()) // Use merge to avoid overwriting other fields
            .await()
    }

    suspend fun getUserFavoriteNovels(userId: String): List<String> {
        val document = db.collection("userPreferences")
            .document(userId)
            .get()
            .await()
        return document.get("favoriteNovels") as? List<String> ?: emptyList()
    }




}