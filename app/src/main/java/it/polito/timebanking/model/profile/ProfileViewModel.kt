package it.polito.timebanking.model.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    fun get(id: String): LiveData<ProfileData> {
        val user = MutableLiveData<ProfileData>()

        FirebaseFirestore.getInstance().collection("users").document(id).addSnapshotListener { r, _ ->
                if (r != null) {
                    user.value = r.toUserProfileData()
                }
            }
        return user
    }


    fun update(
        id: String, fullName: String?, nickname: String?, age: Long?, email: String?, location: String?, description: String?
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
        FirebaseFirestore.getInstance().collection("users").document(id).update(data as Map<String, Any>)
    }
}