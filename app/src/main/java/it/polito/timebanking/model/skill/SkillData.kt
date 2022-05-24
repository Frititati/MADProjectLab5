package it.polito.timebanking.model.timeslot

import com.google.firebase.firestore.DocumentSnapshot

class SkillData(
    val title: String
)

fun DocumentSnapshot.toSkillData() = SkillData(this.get("title").toString())
