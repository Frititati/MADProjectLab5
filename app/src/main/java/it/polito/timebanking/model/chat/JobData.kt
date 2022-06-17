package it.polito.timebanking.model.chat

import it.polito.timebanking.ui.messages.JobStatus
import com.google.firebase.firestore.DocumentSnapshot

data class JobData(
    var timeslotID: String,
    var messagesList: List<*>,
    var lastUpdate: Long,
    var userProducerID: String,
    var userConsumerID: String,
    var users: List<*>,
    var jobStatus: JobStatus,
    var ratingProducer: String,
    var ratingConsumer: String,
    var seenByProducer: Boolean,
    var seenByConsumer: Boolean
)

fun DocumentSnapshot.toJobData(): JobData {
    return JobData(
        this.getString("timeslotID") ?: "",
        this.get("messagesList") as List<*>? ?: emptyList<String>(),
        this.getLong("lastUpdate") ?: 0,
        this.getString("userProducerID") ?: "",
        this.getString("userConsumerID") ?: "",
        this.get("users") as List<*>? ?: emptyList<String>(),
        JobStatus.valueOf(this.getString("jobStatus") ?: "INIT"),
        this.getString("ratingProducer") ?: "",
        this.getString("ratingConsumer") ?: "",
        this.getBoolean("seenByProducer") ?: false,
        this.getBoolean("seenByConsumer") ?: false
    )
}
