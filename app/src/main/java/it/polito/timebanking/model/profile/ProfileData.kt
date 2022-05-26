package it.polito.timebanking.model.profile

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.getField

data class ProfileData(
    var fullName: String,
    var nickName: String,
    var email: String,
    var age: Long,
    var location: String,
    var skills: List<String>,
    var timeslots: List<*>,
    var description: String,
    var favorites: List<*>,
    var time: Long
)

fun DocumentSnapshot.toUserProfileData(): ProfileData {
    return ProfileData(
        this.getString("fullName") ?: "",
        this.getString("nickName") ?: "",
        this.getString("email") ?: "",
        this.getLong("age") ?: 0,
        this.getString("location") ?: "",
        this.get("skills") as List<String>,
        this.get("timeslots") as List<*>? ?: emptyList<Any>(),
        this.getString("description") ?: "",
        this.get("favorites") as List<*>,
        this.getLong("time") ?: 0
    )
}

fun fullNameFormatter(fullName: String?): String {
    return if (fullName.isNullOrEmpty()) "No Name"
    else fullName
}

fun nickNameFormatter(nickName: String?): String {
    return if (nickName.isNullOrEmpty()) "No NickName"
    else nickName
}

fun locationFormatter(location: String?): String {
    return if (location.isNullOrEmpty()) "Empty Location"
    else location
}

fun descriptionFormatter(description: String?): String {
    return if (description.isNullOrEmpty()) "Empty Description"
    else description
}

fun emailFormatter(email: String?): String {
    return if (email.isNullOrEmpty()) "Empty Email"
    else email
}

fun ageFormatter(age: String?): String {
    return if (age.isNullOrEmpty()) "Empty Age"
    else age
}
