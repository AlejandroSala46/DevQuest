package com.example.devquest.ui.theme

import android.os.Parcel
import android.os.Parcelable

data class Pocion(var tipo: TipoPocion) : Parcelable {

    val type: String = tipo.name
    val nombre: String = tipo.getNombre()
    val estante: Int = tipo.getEstante()
    val imagen: Int = tipo.getImagen()

    override fun toString(): String {
        return "Pocion{" +
                "nombre='" + nombre + '\'' +
                ", estante=" + estante +
                '}'
    }

    constructor(parcel: Parcel) : this(
        TipoPocion.valueOf(parcel.readString() ?: TipoPocion.values().first().name)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tipo.name) // Guardamos el nombre del enum
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Pocion> {
        override fun createFromParcel(parcel: Parcel): Pocion = Pocion(parcel)
        override fun newArray(size: Int): Array<Pocion?> = arrayOfNulls(size)
    }
}
