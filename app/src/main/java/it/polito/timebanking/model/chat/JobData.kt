package it.polito.timebanking.model.chat

import com.google.firebase.firestore.DocumentSnapshot

data class JobData(
    var timeslotID: String?,
    var messagesList: List<*>,
    var lastMessage: Long?,
    var userProducerID: String?,
    var userConsumerID: String?,
    var users: List<*>,
    var requestedAt: Long?,
    var requestStatus: String?,
    var jobStatus: String?,
    var ratingProducer : String?,
    var ratingConsumer: String?,
)

fun DocumentSnapshot.toJobData(): JobData {
    return JobData(
        this.getString("timeslotID"),
        this.get("messagesList") as List<*>,
        this.getLong("lastMessage"),
        this.getString("userProducerID"),
        this.getString("userConsumerID"),
        this.get("users") as List<*>,
        this.getLong("requestedAt"),
        this.getString("requestStatus"),
        this.getString("jobStatus"),
        this.getString("ratingProducer"),
        this.getString("ratingConsumer")
    )
}

fun pair(input: List<*>): Pair<String, String> {
    return Pair(input[0].toString(), input[1].toString())
}