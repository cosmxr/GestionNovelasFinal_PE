package com.example.gestionnovelasfinal

import android.os.Parcel
import android.os.Parcelable


data class Resenas(
    val nombre: String,
    val contenido: String
) : Parcelable {
    constructor(parcel: String) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(contenido)
        parcel.writeString(nombre)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Resenas> {
        override fun createFromParcel(parcel: Parcel): Resenas {
            return Resenas(parcel.toString())
        }

        override fun newArray(size: Int): Array<Resenas?> {
            return arrayOfNulls(size)
        }
    }
}

private fun String.readString(): String? {
    TODO("Not yet implemented")
}
