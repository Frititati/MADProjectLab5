package it.polito.timebanking.ui.offers

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
import it.polito.timebanking.model.skill.SkillData

class AllSkillsAdapter : RecyclerView.Adapter<AllSkillsAdapter.SkillListViewHolder>() {
    private var allSkills: MutableList<Pair<String, SkillData>> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillListViewHolder {
        return SkillListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_all_skills, parent, false))
    }

    override fun onBindViewHolder(holder: SkillListViewHolder, position: Int) {
        val temp = allSkills[position]
        holder.bind(temp.first, temp.second)
    }

    override fun getItemCount() = allSkills.size

    @SuppressLint("NotifyDataSetChanged")
    fun setSkills(skills: MutableList<Pair<String, SkillData>>) {
        allSkills = skills.sortedBy { it.second.title }.toMutableList()
        notifyDataSetChanged()
    }

    class SkillListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val rootView = v
        val title = v.findViewById<TextView>(R.id.title)!!


        fun bind(id: String, skill: SkillData) {
            title.text = skill.title
            title.setOnClickListener {
                rootView.findNavController().navigate(R.id.skill_to_offers, bundleOf("skill_select" to id, "offerName" to title.text))
                Snackbar.make(it, "You are seeing all offers for ${title.text}", 1500).show()
            }
        }
    }
}