package com.example.devquest.ui.theme

import android.os.Parcel
import android.os.Parcelable

data class User(
    val name: String,
    val email: String,
    val password: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",  // Asegúrate de leer un valor no nulo
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(password)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(parcel: Parcel): User {
                return User(parcel)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }
    }
}
