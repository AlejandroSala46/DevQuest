package com.example.devquest.ui.theme

import android.os.Parcel
import android.os.Parcelable

data class Level(
    val id: Int,
    val name: String,
    val description: String,
    val maxPuntacion: Int,
    val listPotions: HashMap<String, Int>,
    val listCommands: List<String>,
    var isCompleted: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        readStringIntMap(parcel),
        readStringList(parcel),  // Cambiado para manejar listCommands
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeInt(maxPuntacion)
        writeStringIntMap(parcel, listPotions)
        writeStringList(parcel, listCommands)  // Cambiado para manejar listCommands
        parcel.writeByte(if (isCompleted) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Level> {
        override fun createFromParcel(parcel: Parcel): Level = Level(parcel)
        override fun newArray(size: Int): Array<Level?> = arrayOfNulls(size)

        // Función para leer el HashMap de String a Int
        private fun readStringIntMap(parcel: Parcel): HashMap<String, Int> {
            val size = parcel.readInt()
            val map = HashMap<String, Int>(size)
            repeat(size) {
                val key = parcel.readString() ?: ""
                val value = parcel.readInt()
                map[key] = value
            }
            return map
        }

        // Función para escribir el HashMap de String a Int
        private fun writeStringIntMap(parcel: Parcel, map: Map<String, Int>) {
            parcel.writeInt(map.size)
            for ((key, value) in map) {
                parcel.writeString(key)
                parcel.writeInt(value)
            }
        }

        // Nueva función para leer una lista de String
        private fun readStringList(parcel: Parcel): List<String> {
            val size = parcel.readInt()
            val list = mutableListOf<String>()
            repeat(size) {
                list.add(parcel.readString() ?: "")
            }
            return list
        }

        // Nueva función para escribir una lista de String
        private fun writeStringList(parcel: Parcel, list: List<String>) {
            parcel.writeInt(list.size)
            for (item in list) {
                parcel.writeString(item)
            }
        }
    }
}
