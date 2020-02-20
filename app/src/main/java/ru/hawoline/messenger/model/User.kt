package ru.hawoline.messenger.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public class User(val uid: String, public val username: String, val profilePhoto: String): Parcelable {
    constructor() : this("", "", "")
}