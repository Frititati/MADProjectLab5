package it.polito.timebanking.model.profile

import com.google.firebase.firestore.DocumentSnapshot

data class UserProfileData(
    var fullName: String?,
    var nickName: String?,
    var email: String?,
    var age: Int?,
    var location: String?,
    var skills: List<String>,
    var description: String?
)

fun DocumentSnapshot.toUserProfileData(): UserProfileData {
    return UserProfileData(
        this.get("fullName").toString(),
        this.get("nickName").toString(),
        this.get("email").toString(),
        this.get("age").toString().toIntOrNull(),
        this.get("location").toString(),
        this.get("skills") as List<String>,
        this.get("description").toString()
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

fun emailFormatter(email:String?): String{
    return if (email.isNullOrEmpty()) "Empty Email"
    else email
}

fun ageFormatter(age: String?): String {
    return if (age.isNullOrEmpty()) "Empty Age"
    else age
}
