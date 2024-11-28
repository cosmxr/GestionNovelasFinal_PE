package com.example.gestionnovelasfinal

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    suspend fun actualizarNovela(novela: Novela) {
        db.collection("novelasClasicas")
            .document(novela.id)
            .set(novela)
            .await()
    }

    suspend fun agregarResena(novelaId: String, nombreNovela: String, resena: Resenas) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db.collection("Resenas")
            .add(resena.copy(novelaId = novelaId, nombre = nombreNovela))
            .await()
    }

    suspend fun eliminarResena(resena: Resenas) {
        db.collection("Resenas")
            .whereEqualTo("novelaId", resena.novelaId)
            .whereEqualTo("nombre", resena.nombre)
            .whereEqualTo("contenido", resena.contenido)
            .get()
            .await()
            .documents.forEach { document ->
                db.collection("Resenas").document(document.id).delete().await()
            }
    }

    suspend fun obtenerResenas(): List<Resenas> {
        return db.collection("Resenas")
            .get()
            .await()
            .documents.map { document ->
                document.toObject(Resenas::class.java)!!
            }
    }
    fun getUserId(): String? {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.uid
    }
}