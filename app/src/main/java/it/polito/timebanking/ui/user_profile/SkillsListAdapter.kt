package it.polito.timebanking.ui.user_profile

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebanking.R
import it.polito.timebanking.model.timeslot.SkillData


class SkillsListAdapter : RecyclerView.Adapter<SkillsListAdapter.SkillsListViewHolder>() {
    private var allSkills: Map<String, SkillData> = emptyMap()
    private var userSkills: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillsListViewHolder {
        return SkillsListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.skills_list_widget, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SkillsListViewHolder, position: Int) {
        if (userSkills.isNotEmpty() && allSkills.isNotEmpty()) {
            holder.bind(allSkills[userSkills[position]]!!.title)
        }
    }

    override fun getItemCount() = userSkills.size

    @SuppressLint("NotifyDataSetChanged")
    fun setUserSkills(inUserSkills: List<String>) {
        userSkills = inUserSkills as MutableList<String>
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setAllSkills(inAllSkills: Map<String, SkillData>) {
        allSkills = inAllSkills
        notifyDataSetChanged()
    }

    fun setEmptyLists() {
        allSkills = emptyMap()
        userSkills.clear()
    }

    class SkillsListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val sv: TextView? = v.findViewById(R.id.skill)
        fun bind(skillName: String) {
            sv!!.text = skillName
        }
    }
}