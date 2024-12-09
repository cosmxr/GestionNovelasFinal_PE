package com.example.gestionnovelasfinal


import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.PropertyName

data class Novela(
    val id: String = "", // Este campo será llenado automáticamente por Firestore.
    val nombre: String = "", // Valor por defecto
    val autor: String = "", // Valor por defecto
    val año_publicacion: Int = 0, // Valor por defecto
    val descripcion: String = "", // Valor por defecto
    val resenas: List<Resenas> = emptyList(), // Cambiado a List<Resenas>
    val latitude: Double? = null,
    val longitude: Double? = null,
    @get:PropertyName("isFavorita") @set:PropertyName("isFavorita") var isFavorita: Boolean = false

) : Parcelable {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        mutableListOf<Resenas>().apply {
            parcel.readTypedList(this, Resenas.CREATOR)
        },
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readBoolean() // Asegúrate de leer el valor de isFavorita también
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nombre)
        parcel.writeString(autor)
        parcel.writeInt(año_publicacion)
        parcel.writeString(descripcion)
        parcel.writeTypedList(resenas) // Asegúrate de escribir la lista correctamente
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeBoolean(isFavorita) // Escribir isFavorita
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Novela> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Novela {
            return Novela(parcel)
        }

        override fun newArray(size: Int): Array<Novela?> {
            return arrayOfNulls(size)
        }
    }
}
