package com.example.gestionnovelasfinal

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference

class FirebaseConfig {

    // Instancia de Firebase Firestore
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Obtener la referencia de la colección de Novelas
    fun getNovelasCollection(): CollectionReference {
        return db.collection("Novelas")
    }

    // Método para obtener todas las novelas
    fun obtenerNovelas(onSuccess: (List<Novela>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("Novelas")
            .get()
            .addOnSuccessListener { result ->
                val listaNovelas = mutableListOf<Novela>()
                for (document in result) {
                    val titulo = document.getString("titulo") ?: ""
                    val autor = document.getString("autor") ?: ""
                    val ano = document.getLong("ano")?.toInt() ?: 0
                    val descripcion = document.getString("descripcion") ?: ""
                    val novela = Novela(titulo, autor, ano, descripcion)
                    listaNovelas.add(novela)
                }
                // Llamar al callback de éxito
                onSuccess(listaNovelas)
            }
            .addOnFailureListener { exception ->
                // Llamar al callback de error
                onFailure(exception)
            }
    }

    // Método para agregar una novela a la colección
    fun agregarNovela(novela: Novela, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("Novelas")
            .add(novela)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
