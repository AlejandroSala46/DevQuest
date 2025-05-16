package com.example.devquest.ui.theme

import android.os.Parcel
import android.os.Parcelable

data class User(
    val auth: String,
    val email: String,
    val username: String,
    val role: String,
    //val password: String,
    val levels_completed: List<Level> // Lista de niveles completos
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        readLevelList(parcel)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(auth)
        parcel.writeString(email)
        parcel.writeString(username)
        parcel.writeString(role)
        writeLevelList(parcel, levels_completed) // Método auxiliar para escribir la lista de niveles
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)
        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)

        private fun readLevelList(parcel: Parcel): List<Level> {
            val size = parcel.readInt()
            val list = mutableListOf<Level>()
            repeat(size) {
                list.add(parcel.readParcelable<Level>(Level::class.java.classLoader) ?: Level(0, "", "", 0, hashMapOf(), emptyList(), false))
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
