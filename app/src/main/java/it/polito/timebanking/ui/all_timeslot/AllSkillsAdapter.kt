package it.polito.timebanking.ui.all_timeslot

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.polito.timebanking.R
import it.polito.timebanking.model.timeslot.SkillData

class AllSkillsAdapter: RecyclerView.Adapter<AllSkillsAdapter.SkillListViewHolder>() {
    private var skills: List<Pair<String, SkillData>> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillListViewHolder {
        return SkillListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.widget_all_skills, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SkillListViewHolder, position: Int) {
        val temp = skills[position]
        holder.bind(temp.first, temp.second)
    }

    override fun getItemCount() = skills.size

    @SuppressLint("NotifyDataSetChanged")
    fun setSkills(in_skills: List<Pair<String, SkillData>>) {
        skills = in_skills.toList()
        notifyDataSetChanged()
    }

    class SkillListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val rootView = v
        val title: TextView = v.findViewById(R.id.title)


        fun bind(id: String, skill: SkillData) {
            title.text = skill.title
            title.setOnClickListener {
                rootView.findNavController().navigate(R.id.skill_to_timeslot, bundleOf("skill_select" to id, "offerName" to "Offers for ${title.text}"))
                Snackbar.make(it, "Here you can view all users with: ${title.text}", 2500)
                    .show()
            }
        }
    }
}