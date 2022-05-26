package it.polito.timebanking.model.chat

import JobStatus
import com.google.firebase.firestore.DocumentSnapshot

data class JobData(
    var timeslotID: String,
    var messagesList: List<*>,
    var lastMessage: Long,
    var userProducerID: String,
    var userConsumerID: String,
    var users: List<*>,
    var jobStatus: JobStatus,
    var ratingProducer: String,
    var ratingConsumer: String,
)

fun DocumentSnapshot.toJobData(): JobData {
    return JobData(
        this.getString("timeslotID") ?: "",
        this.get("messagesList") as List<*>? ?: emptyList<Any>(),
        this.getLong("lastMessage") ?: 0,
        this.getString("userProducerID") ?: "",
        this.getString("userConsumerID") ?: "",
        this.get("users") as List<*>? ?: emptyList<Any>(),
        JobStatus.valueOf(this.getString("jobStatus") ?: "INIT"),
        this.getString("ratingProducer") ?: "",
        this.getString("ratingConsumer") ?: ""
    )
}
