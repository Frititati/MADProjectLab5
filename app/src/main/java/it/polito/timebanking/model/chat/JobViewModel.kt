package it.polito.timebanking.model.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class JobViewModel(application: Application) : AndroidViewModel(application) {

    fun getJob(user:String): LiveData<List<Pair<String, JobData>>> {
        val chats = MutableLiveData<List<Pair<String, JobData>>>()
        FirebaseFirestore.getInstance().collection("jobs").whereArrayContains("users",user)
            .addSnapshotListener { r, e ->
                if (r != null) {
                    chats.value = if (e != null)
                        emptyList()
                    else r.mapNotNull { Pair(it.id, it.toJobData()) }
                }
            }
        return chats
    }
}