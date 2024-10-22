package com.example.gestionnovelasfinal

import android.os.Parcel
import android.os.Parcelable

data class Resenas(
    val novelaId: String = "",
    val nombre: String = "",
    val contenido: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(novelaId)
        parcel.writeString(nombre)
        parcel.writeString(contenido)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Resenas> {
        override fun createFromParcel(parcel: Parcel): Resenas {
            return Resenas(parcel)
        }

        override fun newArray(size: Int): Array<Resenas?> {
            return arrayOfNulls(size)
        }
    }
}