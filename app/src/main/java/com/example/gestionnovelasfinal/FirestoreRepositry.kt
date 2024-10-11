package com.example.gestionnovelasfinal

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun agregarNovela(novela: Novela) {
        db.collection("novelasClasicas")
            .add(novela)
            .await() // Espera a que la tarea se complete
    }

    suspend fun obtenerNovelas(): List<Novela> {
        return db.collection("novelasClasicas")
            .get()
            .await()
            .documents.map { document ->
                document.toObject(Novela::class.java)!!.copy(id = document.id) // Aquí asignas el ID
            }
    }

}
