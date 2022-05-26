package it.polito.timebanking.model.skill

import com.google.firebase.firestore.DocumentSnapshot

class SkillData(
    val title: String
)

fun DocumentSnapshot.toSkillData() = SkillData(this.getString("title") ?: "")
