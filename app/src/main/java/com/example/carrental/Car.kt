package com.example.carrental

import android.os.Parcel
import android.os.Parcelable

data class Car(
    val id: Int,
    val name: String,
    val model: String,
    val year: Int,
    val rating: Float,
    val km: Int,
    val dailyCost: Int,
    val imageRes: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    // Fields must be written in the same order they are read above
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(model)
        parcel.writeInt(year)
        parcel.writeFloat(rating)
        parcel.writeInt(km)
        parcel.writeInt(dailyCost)
        parcel.writeInt(imageRes)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Car> {
        override fun createFromParcel(parcel: Parcel) = Car(parcel)
        override fun newArray(size: Int): Array<Car?> = arrayOfNulls(size)
    }
}

data class Booking(val car: Car, val days: Int, val renter: String) {
    val total get() = car.dailyCost * days
}