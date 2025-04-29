package com.example.devquest.ui.theme

import android.os.Parcel
import android.os.Parcelable

data class User(
    val name: String,
    val email: String,
    val password: String,
    val LevelsComplete: List<Level> // Lista de niveles completos
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        readLevelList(parcel) // Método auxiliar para leer la lista de niveles
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(password)
        writeLevelList(parcel, LevelsComplete) // Método auxiliar para escribir la lista de niveles
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)
        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)

        private fun readLevelList(parcel: Parcel): List<Level> {
            val size = parcel.readInt()
            val list = mutableListOf<Level>()
            repeat(size) {
                list.add(parcel.readParcelable<Level>(Level::class.java.classLoader) ?: Level(0, "", "", hashMapOf(), emptyList(), false))
            }
            return list
        }

        private fun writeLevelList(parcel: Parcel, list: List<Level>) {
            parcel.writeInt(list.size)
            for (level in list) {
                parcel.writeParcelable(level, 0)
            }
        }
    }
}
