package it.polito.timebanking.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class TimeViewModel(application: Application) : AndroidViewModel(application) {
    fun get(userID: String): LiveData<Long> {
        val time = MutableLiveData<Long>()
        FirebaseFirestore.getInstance().collection("users").document(userID)
            .addSnapshotListener { t, e ->
                if (t != null) {
                    time.value = if (e != null)
                        0L
                    else t.getLong("time") ?: 0L
                }
            }
        return time
    }
}