package it.polito.timebanking.model.chat

import com.google.firebase.firestore.DocumentSnapshot

data class JobData(
    var timeslotID: String,
    var messagesList: List<*>,
    var lastMessage: Long?,
    var userProducerID: String,
    var userConsumerID: String,
    var users: List<*>,
    var requestedAt: Long?,
    var requestStatus: String,
    var jobStatus: String,
    var ratingProducer : String,
    var ratingConsumer: String,
)

fun DocumentSnapshot.toJobData(): JobData {
    return JobData(
        this.get("timeslotID").toString(),
        this.get("messagesList") as List<*>,
        this.get("lastMessage").toString().toLongOrNull(),
        this.get("userProducerID").toString(),
        this.get("userConsumerID").toString(),
        this.get("users") as List<*>,
        this.get("requestedAt").toString().toLongOrNull(),
        this.get("requestStatus").toString(),
        this.get("jobStatus").toString(),
        this.get("ratingProducer").toString(),
        this.get("ratingConsumer").toString()
    )
}

fun pair(input: List<*>): Pair<String, String> {
    return Pair(input[0].toString(), input[1].toString())
}