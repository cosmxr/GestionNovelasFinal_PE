package com.example.gestionnovelasfinal

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun agregarNovela(novela: Novela): Novela {
        val documentReference = db.collection("novelasClasicas")
            .add(novela)
            .await()
        return novela.copy(id = documentReference.id)
    }

    suspend fun obtenerNovelas(): List<Novela> {
        return db.collection("novelasClasicas")
            .get()
            .await()
            .documents.map { document ->
                document.toObject(Novela::class.java)!!.copy(id = document.id)
            }
    }

    suspend fun obtenerNovelasFavoritas(): List<Novela> {
        return db.collection("novelasClasicas")
            .whereEqualTo("isFavorita", true)
            .get()
            .await()
            .documents.map { document ->
                document.toObject(Novela::class.java)!!.copy(id = document.id)
            }
    }

    suspend fun actualizarNovela(novela: Novela) {
        db.collection("novelasClasicas")
            .document(novela.id)
            .set(novela)
            .await()
    }

    suspend fun agregarNovelasFavoritas(novelaId: String, isFavorita: Boolean) {
        db.collection("novelasClasicas")
            .document(novelaId)
            .update("isFavorita", isFavorita)
            .await()
    }

    suspend fun agregarResena(novelaId: String, nombreNovela: String,resena: Resenas) {
        db.collection("resenas")
            .add(resena.copy(novelaId = novelaId, nombre = nombreNovela))
            .await()
    }

    suspend fun obtenerResenas(): List<Resenas> {
        return db.collection("resenas")
            .get()
            .await()
            .documents.map { document ->
                document.toObject(Resenas::class.java)!!
            }
    }}