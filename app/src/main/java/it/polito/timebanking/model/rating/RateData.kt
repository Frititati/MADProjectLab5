package it.polito.timebanking.model.rating

import com.google.firebase.firestore.DocumentSnapshot

data class RateData(
    var score: Double,
    var comment: String,
    var jobName: String,
    var senderID: String,
    var receiverID: String,
    var consumerRate: Boolean
)

fun DocumentSnapshot.toRateData(): RateData {
    return RateData(
        this.getDouble("score") ?: .0,
        this.getString("comment") ?: "",
        this.getString("jobName") ?: "",
        this.getString("senderID") ?: "",
        this.getString("receiverID") ?: "",
        this.getBoolean("consumerRate") ?: false
        )

}
