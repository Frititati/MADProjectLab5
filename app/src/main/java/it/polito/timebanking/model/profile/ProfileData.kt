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
    var time: Long,
    var activeConsumingJobs: Long,
    var activeProducingJobs: Long,
    var scoreAsProducer: Double,
    var jobsRatedAsProducer: Long,
    var scoreAsConsumer: Double,
    var jobsRatedAsConsumer: Long,
    var usedCoupons: List<*>
)

fun DocumentSnapshot.toUserProfileData(): ProfileData {
    return ProfileData(
        this.getString("fullName") ?: "",
        this.getString("nickName") ?: "",
        this.getString("email") ?: "",
        this.getLong("age") ?: 0,
        this.getString("location") ?: "",
        this.get("skills") as List<*> ? ?: emptyList<String>(),
        this.get("timeslots") as List<*>? ?: emptyList<String>(),
        this.getString("description") ?: "",
        this.getLong("time") ?: 0,
        this.getLong("activeConsumingJobs") ?: 0,
        this.getLong("activeProducingJobs") ?: 0,
        this.getDouble("scoreAsProducer") ?: .0,
        this.getLong("jobsRatedAsProducer") ?: 0,
        this.getDouble("scoreAsConsumer") ?: .0,
        this.getLong("jobsRatedAsConsumer") ?: 0,
        this.get("usedCoupons") as List<*>? ?: emptyList<String>(),
    )
}

fun fullNameFormatter(fullName: String?, insertion: Boolean): String {
    if (insertion){
        return if (fullName.isNullOrEmpty()) "Insert FullName"
        else fullName
    }
    return if (fullName.isNullOrEmpty()) "No Name"
    else fullName
}

fun nickNameFormatter(nickName: String?, insertion: Boolean): String {
    if (insertion){
        return if (nickName.isNullOrEmpty()) "Insert NickName"
        else nickName
    }
    return if (nickName.isNullOrEmpty()) "No NickName"
    else nickName
}

fun locationFormatter(location: String?, insertion: Boolean): String {
    if (insertion){
        return if (location.isNullOrEmpty()) "Insert Location"
        else location
    }
    return if (location.isNullOrEmpty()) "No Location"
    else location
}

fun descriptionFormatterProfile(description: String?, insertion: Boolean): String {
    if (insertion){
        return if (description.isNullOrEmpty()) "Insert Description"
        else description
    }
    return if (description.isNullOrEmpty()) "No Description"
    else description
}

fun emailFormatter(email: String?, insertion: Boolean): String {
    if (insertion){
        return if (email.isNullOrEmpty()) "Insert Email"
        else email
    }
    return if (email.isNullOrEmpty()) "No Email"
    else email
}

fun ageFormatter(age: Long?, insertion: Boolean): String {
    if (insertion){
        return if (age == 0L) "Insert Age"
        else String.format("%d Years Old", age)
    }
    return if (age == 0L) "No Age"
    else String.format("%d Years Old", age)
}
