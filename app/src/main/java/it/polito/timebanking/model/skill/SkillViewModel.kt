package it.polito.timebanking.model.skill

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class SkillViewModel(application: Application) : AndroidViewModel(application) {

    fun get(): LiveData<List<Pair<String, SkillData>>> {
        val skills = MutableLiveData<List<Pair<String, SkillData>>>()

        FirebaseFirestore.getInstance().collection("skills")
            .addSnapshotListener { s, e ->
                if (s != null) {
                    skills.value = if (e != null)
                        emptyList()
                    else s.map { Pair(it.id, it.toSkillData()) }
                }
            }
        return skills
    }
}