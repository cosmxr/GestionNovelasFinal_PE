package com.example.gestionnovelasfinal

import android.content.Context
import java.io.File

class SharedPreferences(private val context: Context, private val userId: String) {

    fun guardarResena(resena: Resenas) {
        val file = File(context.filesDir, "$userId-${resena.novelaId}.txt")
        file.appendText("${resena.nombre}: ${resena.contenido}\n")
    }

    fun obtenerResenas(novelaId: String): List<Resenas> {
        val file = File(context.filesDir, "$userId-$novelaId.txt")
        if (!file.exists()) return emptyList()

        return file.readLines().map { line ->
            val parts = line.split(": ")
            Resenas(novelaId, parts[0], parts[1])
        }
    }
}