package it.polito.timebanking.model.timeslot

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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

    fun getTimeslotsForSkill(skill: String): LiveData<List<Pair<String, TimeslotData>>> {
        val offers = MutableLiveData<List<Pair<String, TimeslotData>>>()

        FirebaseFirestore.getInstance().collection("timeslots").whereArrayContains("skills", skill)
            .whereEqualTo("available", true)
            .addSnapshotListener { u, _ ->
                if (u != null) {
                    offers.value = u.filter { it.toTimeslotData().date + 86399999 >= System.currentTimeMillis() }
                        .map { Pair(it.id, it.toTimeslotData()) }
                }
            }
        return offers
    }

    fun getUserTimeslots(): LiveData<List<Pair<String, TimeslotData>>> {
        val timeslots = MutableLiveData<List<Pair<String, TimeslotData>>>()

        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .addSnapshotListener { ut, _ ->
                val timeslotsList: MutableList<Pair<String, TimeslotData>> = mutableListOf()
                (ut?.get("timeslots") as List<*>).forEach {
                    FirebaseFirestore.getInstance().collection("timeslots")
                        .document(it.toString())
                        .addSnapshotListener { t, _ ->
                            if (t != null) {
                                timeslotsList.add(Pair(t.id, t.toTimeslotData()))
                                timeslots.value = timeslotsList
                            }
                        }
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
        location: String
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

        data["editedAt"] = System.currentTimeMillis()

        FirebaseFirestore.getInstance().collection("timeslots").document(id).update(
            data as Map<String, Any>
        )
    }

    fun delete(id: String) {
        FirebaseFirestore.getInstance().collection("timeslots").document(id).delete()
            .addOnSuccessListener { }
            .addOnFailureListener { e -> Log.w("test", "Error deleting document", e) }
    }

    fun deleteUserTimeslot(id: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid).update(
                "timeslots", FieldValue.arrayRemove(id)
            ).addOnSuccessListener {
            }
    }
}
