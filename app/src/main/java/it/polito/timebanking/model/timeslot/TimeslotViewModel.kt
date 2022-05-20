package it.polito.timebanking.model.timeslot

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TimeslotViewModel(application: Application) : AndroidViewModel(application) {

    fun get(id: String): LiveData<TimeslotData> {
        val timeslot = MutableLiveData<TimeslotData>()

        FirebaseFirestore.getInstance().collection("timeslots").document(id)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    timeslot.value = r.toTimeslotData()
                }
            }
        return timeslot
    }

    fun getUserTimeslots(id: String): LiveData<List<Pair<String, TimeslotData>>> {
        val timeslots = MutableLiveData<List<Pair<String, TimeslotData>>>()

        FirebaseFirestore.getInstance().collection("timeslots").whereEqualTo("ownedBy", id)
            .addSnapshotListener { r, e ->
                if (r != null) {
                    timeslots.value = if (e != null)
                        emptyList()
                    else r.mapNotNull { Pair(it.id, it.toTimeslotData()) }
                }
            }
        return timeslots
    }

    fun update(
        id: String,
        title: String,
        description: String,
        date: Long,
        duration: String,
        location: String,
        skills: List<String>
    ) {
        val data: MutableMap<String, Any> = mutableMapOf()
        if (title.isNotEmpty()) {
            data["title"] = title
        }

        if (description.isNotEmpty()) {
            data["description"] = description
        }
        if (date != 0L) {
            data["date"] = date
        }

        if (duration.isNotEmpty()) {
            data["duration"] = duration.toInt()
        }

        if (location.isNotEmpty()) {
            data["location"] = location
        }

        if (skills.isNotEmpty()) {
            data["skills"] = skills
        }

        data["editedAt"] = System.currentTimeMillis()

        FirebaseFirestore.getInstance().collection("timeslots").document(id).update(
            data as Map<String, Any>
        )
    }

    fun delete(id: String) {
        FirebaseFirestore.getInstance().collection("timeslots").document(id).delete()
            .addOnSuccessListener { Log.d("test", "Cancelled $id") }
            .addOnFailureListener { e -> Log.w("test", "Error deleting document", e) }
    }
}
