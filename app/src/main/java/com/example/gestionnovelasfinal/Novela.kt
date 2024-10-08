package com.example.gestionnovelasfinal


import android.os.Parcel
import android.os.Parcelable

data class Novela(
    val titulo: String,
    val autor: String,
    val ano: Int,
    val descripcion: String,
    val resenas: MutableList<Resenas> = mutableListOf(),
    var isFavorita: Boolean = false
) : Parcelable //Las hago Parceable para que esta pueda ser añadida de manera correcta
// y las reseñas se añadan correctamente
{

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        mutableListOf<Resenas>().apply {
            parcel.readTypedList(this, Resenas.CREATOR)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(titulo)
        parcel.writeString(autor)
        parcel.writeInt(ano)
        parcel.writeString(descripcion)
        parcel.writeTypedList(resenas)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Novela> {
        override fun createFromParcel(parcel: Parcel): Novela {
            return Novela(parcel)
        }

        override fun newArray(size: Int): Array<Novela?> {
            return arrayOfNulls(size)
        }
    }
}
