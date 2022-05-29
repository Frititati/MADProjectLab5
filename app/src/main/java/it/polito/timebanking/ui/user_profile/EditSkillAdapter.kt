package it.polito.timebanking.ui.user_profile

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue.arrayRemove
import com.google.firebase.firestore.FieldValue.arrayUnion
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.model.skill.SkillData

class EditSkillAdapter : RecyclerView.Adapter<EditSkillAdapter.SkillListViewHolder>() {
    private var allSkills = mutableListOf<Pair<String, SkillData>> ()
    private var userSkills = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillListViewHolder {
        return SkillListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_edit_profile_skill, parent, false))
    }

    override fun onBindViewHolder(holder: SkillListViewHolder, position: Int) {
        if (allSkills.isNotEmpty()) {
            val temp = allSkills[position]
            holder.bind(temp.first, temp.second, temp.first in userSkills)
        }
    }

    override fun getItemCount() = allSkills.size

    @SuppressLint("NotifyDataSetChanged")
    fun setSkills(inAllSkills: List<Pair<String, SkillData>>) {
        allSkills = inAllSkills.sortedBy { it.second.title }.toMutableList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setUserSkills(inUserSkills: List<String>) {
        userSkills = inUserSkills as MutableList<String>
        notifyDataSetChanged()
    }

    fun setEmptyLists() {
        allSkills.clear()
        userSkills.clear()
    }

    class SkillListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val checkBox = v.findViewById<CheckBox>(R.id.checkBox)

        private val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        fun bind(id: String, skill: SkillData, is_selected: Boolean) {
            checkBox.text = skill.title
            checkBox.isChecked = is_selected
            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    addSkillUser(id)
                } else {
                    removeSkillUser(id)
                }
            }
        }

        private fun addSkillUser(skillID: String) {
            FirebaseFirestore.getInstance().collection("users").document(firebaseUserID).update("skills", arrayUnion(skillID))
        }

        private fun removeSkillUser(skillID: String) {
            FirebaseFirestore.getInstance().collection("users").document(firebaseUserID).update("skills", arrayRemove(skillID))
        }
    }
}