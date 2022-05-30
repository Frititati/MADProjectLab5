package it.polito.timebanking.model.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class JobViewModel(application: Application) : AndroidViewModel(application) {

    fun getProducingJobs(user: String): LiveData<List<Pair<String, JobData>>> {
        val jobs = MutableLiveData<List<Pair<String, JobData>>>()
        FirebaseFirestore.getInstance().collection("jobs").whereEqualTo("userProducerID", user).addSnapshotListener { j, e ->
            if (j != null) {
                jobs.value = if (e != null) emptyList()
                else j.map { Pair(it.id, it.toJobData()) }
            }
        }
        return jobs
    }

    fun getConsumingJobs(user: String): LiveData<List<Pair<String, JobData>>> {
        val jobs = MutableLiveData<List<Pair<String, JobData>>>()
        FirebaseFirestore.getInstance().collection("jobs").whereEqualTo("userConsumerID", user).addSnapshotListener { r, e ->
            if (r != null) {
                jobs.value = if (e != null) emptyList()
                else r.map { Pair(it.id, it.toJobData()) }
            }
        }
        return jobs
    }

    fun getCompletedJobs(user: String): LiveData<List<Pair<String, JobData>>> {
        val jobs = MutableLiveData<List<Pair<String, JobData>>>()
        FirebaseFirestore.getInstance().collection("jobs").whereArrayContains("users", user).whereEqualTo("jobStatus", "COMPLETED").addSnapshotListener { r, e ->
            if (r != null) {
                jobs.value = if (e != null) emptyList()
                else r.map { Pair(it.id, it.toJobData()) }
            }
        }
        return jobs
    }

    fun get(jobID: String): LiveData<JobData> {
        val job = MutableLiveData<JobData>()
        FirebaseFirestore.getInstance().collection("jobs").document(jobID).addSnapshotListener { r, _ ->
            if (r != null) {
                job.value = r.toJobData()
            }
        }
        return job
    }
}