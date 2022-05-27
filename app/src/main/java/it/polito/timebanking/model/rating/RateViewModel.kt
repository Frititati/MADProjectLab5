package it.polito.timebanking.model.rating

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class RateViewModel(application: Application) : AndroidViewModel(application) {
    fun getRatings(userID: String): LiveData<List<Pair<String, RateData>>> {
        val ratings = MutableLiveData<List<Pair<String, RateData>>>()

        FirebaseFirestore.getInstance().collection("ratings")
            .addSnapshotListener { r, e ->
                if (r != null) {
                    ratings.value = if (e != null)
                        emptyList()
                    else r.filter { it.toRateData().receiverID == userID || it.toRateData().senderID == userID }.map{Pair(it.id,it.toRateData())}
                }
            }
        return ratings
    }
}