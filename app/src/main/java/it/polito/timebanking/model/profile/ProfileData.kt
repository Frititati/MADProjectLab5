package it.polito.timebanking.model.profile

import com.google.firebase.firestore.DocumentSnapshot

data class ProfileData(
    var fullName: String,
    var nickName: String,
    var email: String,
    var age: Long,
    var location: String,
    var skills: List<*>,
    var timeslots: List<*>,
    var description: String,
    var favorites: List<*>,
    var time: Long
)

fun DocumentSnapshot.toUserProfileData(): ProfileData {
    return ProfileData(
        this.get("fullName").toString(),
        this.get("nickName").toString(),
        this.get("email").toString(),
        this.getLong("age").toString().toLong(),
        this.get("location").toString(),
        this.get("skills") as List<*>,
        this.get("timeslots") as List<*>,
        this.get("description").toString(),
        this.get("favorites") as List<*>,
        this.get("time").toString().toLong()
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
