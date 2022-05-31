package it.polito.timebanking.ui.messages

enum class JobStatus {
    INIT, REQUESTED, ACCEPTED, REJECTED, STARTED, DONE, RATED_BY_CONSUMER, RATED_BY_PRODUCER, COMPLETED
}

fun jobStatusFormatter(jobStatus: JobStatus): String {
    return when (jobStatus) {
        JobStatus.INIT -> ""
        JobStatus.REQUESTED -> "Requested"
        JobStatus.ACCEPTED -> "Accepted"
        JobStatus.REJECTED -> "Rejected"
        JobStatus.STARTED -> "Started"
        JobStatus.DONE -> "Done"
        JobStatus.RATED_BY_CONSUMER -> "Rated"
        JobStatus.RATED_BY_PRODUCER -> "Rated"
        JobStatus.COMPLETED -> "Completed"
    }
}