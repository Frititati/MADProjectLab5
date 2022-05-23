package it.polito.timebanking.model.timeslot

import android.content.res.Resources
import com.google.firebase.firestore.DocumentSnapshot
import it.polito.timebanking.R
import java.text.SimpleDateFormat
import java.util.*

data class TimeslotData(
    var createdAt: Long,
    var editedAt: Long,
    var title: String,
    var description: String,
    var date: Long,
    var duration: Int,
    var location: String,
    var ownedBy: String,
    var available: Boolean
)

fun DocumentSnapshot.toTimeslotData(): TimeslotData {
    return TimeslotData(
        this.get("createdAt").toString().toLong(),
        this.get("editedAt").toString().toLong(),
        this.get("title").toString(),
        this.get("description").toString(),
        this.get("date").toString().toLong(),
        this.get("duration").toString().toInt(),
        this.get("location").toString(),
        this.get("ownedBy").toString(),
        this.get("available") as Boolean
    )
}

fun titleFormatter(title: String?): String {
    return if (title.isNullOrEmpty()) "Empty Title"
    else title
}

fun descriptionFormatter(description: String?): String {
    return if (description.isNullOrEmpty()) "Empty Description"
    else description
}

fun durationMinuteFormatter(r: Resources, duration: Int?): String {
    return if (duration == null || duration == 0) String.format(r.getString(R.string.minutes), 0)
    else String.format(r.getString(R.string.minutes), duration)
}

fun durationFormatter(duration: Int?): Int {
    return if (duration == null || duration == 0) 0
    else duration
}

fun locationFormatter(location: String?): String {
    return if (location.isNullOrEmpty()) "Empty Location"
    else location
}

fun dateFormatter(milliSeconds: Long?): String {
    if (milliSeconds == null || milliSeconds == 0L) {
        return "Empty Date"
    }
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).format(calendar.time)
}