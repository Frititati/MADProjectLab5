package it.polito.timebanking.ui.personal_timeslot

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.model.skill.toSkillData

class EditTimeslotSkillAdapter : RecyclerView.Adapter<EditTimeslotSkillAdapter.SkillListViewHolder>() {
    private var availableSkills = mutableListOf<String>()
    private var timeslotSkills = mutableListOf<String>()
    private var timeslotID = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillListViewHolder {
        return SkillListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_edit_profile_skill, parent, false))
    }

    override fun onBindViewHolder(holder: SkillListViewHolder, position: Int) {
        if (availableSkills.isNotEmpty()) {
            holder.bind(availableSkills[position], timeslotID, availableSkills[position] in timeslotSkills)
        }
    }

    override fun getItemCount() = availableSkills.size

    @SuppressLint("NotifyDataSetChanged")
    fun setAvailableSkills(inAllSkills: List<String>) {
        availableSkills = inAllSkills.toMutableList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTimeslotSkills(inTimeslot: String, inUserSkills: List<String>) {
        timeslotID = inTimeslot
        timeslotSkills = inUserSkills as MutableList<String>
        notifyDataSetChanged()
    }

    class SkillListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val checkBox = v.findViewById<CheckBox>(R.id.checkBox)

        fun bind(skillID: String, timeslotID: String, is_selected: Boolean) {
            FirebaseFirestore.getInstance().collection("skills").document(skillID).get().addOnSuccessListener {
                    checkBox.text = it.toSkillData().title
                }
            checkBox.isChecked = is_selected
            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    addSkillTimeslot(skillID, timeslotID)
                } else {
                    removeSkillTimeslot(skillID, timeslotID)
                }
            }
        }

        private fun addSkillTimeslot(skillID: String, timeslotID: String) {
            FirebaseFirestore.getInstance().collection("timeslots").document(timeslotID).update("skills", FieldValue.arrayUnion(skillID))
        }

        private fun removeSkillTimeslot(skillID: String, timeslotID: String) {
            FirebaseFirestore.getInstance().collection("timeslots").document(timeslotID).update("skills", FieldValue.arrayRemove(skillID))
        }
    }
}