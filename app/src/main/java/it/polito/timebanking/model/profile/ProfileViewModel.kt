package it.polito.timebanking.model.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    fun get(id: String): LiveData<ProfileData> {
        val timeslot = MutableLiveData<ProfileData>()

        FirebaseFirestore.getInstance().collection("users").document(id)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    timeslot.value = r.toUserProfileData()
                }
            }
        return timeslot
    }


    fun update(
        id: String,
        fullName: String?,
        nickname: String?,
        age: Long?,
        email: String?,
        location: String?,
        description: String?,
        favList: List<String>
    ) {
        val data = mutableMapOf<String, Any>()

        if (location != null) {
            if (location.isNotEmpty()) {
                data["location"] = location
            }
        }
        if (description != null) {
            if (description.isNotEmpty()) {
                data["description"] = description
            }
        }
        if (nickname != null) {
            if (nickname.isNotEmpty()) {
                data["nickName"] = nickname
            }
        }
        if (email != null) {
            if (email.isNotEmpty()) {
                data["email"] = email
            }
        }
        if (fullName != null) {
            if (fullName.isNotEmpty()) {
                data["fullName"] = fullName
            }
        }
        if (age != null) {
            data["age"] = age
        }
        data["favorites"] = favList
        FirebaseFirestore.getInstance().collection("users").document(id).update(
            data as Map<String, Any>
        )
    }
}