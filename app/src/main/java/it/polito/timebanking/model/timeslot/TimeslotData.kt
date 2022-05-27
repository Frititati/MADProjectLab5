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
    var duration: Long,
    var location: String,
    var ownedBy: String,
    var available: Boolean,
    var skills: List<*>
)

fun DocumentSnapshot.toTimeslotData(): TimeslotData {
    return TimeslotData(
        this.getLong("createdAt") ?: 0L,
        this.getLong("editedAt") ?: 0L,
        this.getString("title") ?: "",
        this.getString("description") ?: "",
        this.getLong("date") ?: 0L,
        this.getLong("duration") ?: 0L,
        this.getString("location") ?: "",
        this.getString("ownedBy") ?: "",
        this.getBoolean("available") ?: false,
        this.get("skills") as List<*>? ?: emptyList<String>()
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

fun durationMinuteFormatter(r: Resources, duration: Long?): String {
    return if (duration == null || duration == 0L) String.format(r.getString(R.string.minutes), 0)
    else String.format(r.getString(R.string.minutes), duration)
}

fun durationFormatter(duration: Long?): Long {
    return if (duration == null || duration == 0L) 0
    else duration
}

fun locationFormatter(location: String?): String {
    return if (location.isNullOrEmpty()) "Empty Location"
    else location
}

fun dateFormatter(milliSeconds: Long?): String {
    if (milliSeconds == null || milliSeconds == 0L) {
        return "No Date"
    }
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).format(calendar.time)
}