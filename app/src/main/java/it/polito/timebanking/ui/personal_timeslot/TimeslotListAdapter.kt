package it.polito.timebanking.ui.personal_timeslot

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebanking.R
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.*

class TimeslotListAdapter : RecyclerView.Adapter<TimeslotListAdapter.TimeslotListViewHolder>() {
    private var timeslots: MutableList<Pair<String, TimeslotData>> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeslotListViewHolder {
        return TimeslotListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.widget_timeslot_list_personal, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TimeslotListViewHolder, position: Int) {
        holder.bind(timeslots[position].first, timeslots[position].second)
    }

    override fun getItemCount() = timeslots.size

    @SuppressLint("NotifyDataSetChanged")
    fun setTimeslots(inTimeslot: MutableList<Pair<String, TimeslotData>>) {
        timeslots = inTimeslot
        notifyDataSetChanged()
    }

    class TimeslotListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val rootView = v
        private val title: TextView = v.findViewById(R.id.title)
        private val location: TextView = v.findViewById(R.id.location)
        private val date: TextView = v.findViewById(R.id.date)
        private val button: TextView = v.findViewById(R.id.details_button)

        fun bind(id: String, timeslot: TimeslotData) {
            title.text = titleFormatter(timeslot.title)
            location.text = locationFormatter(timeslot.location)
            date.text = dateFormatter(timeslot.date)
            rootView.setOnClickListener {
                rootView.findNavController()
                    .navigate(R.id.personal_to_details, bundleOf("id_timeslot" to id))
                Snackbar.make(it, "Details about: ${title.text}", 1500)
                    .show()
            }
            button.setOnClickListener {
                rootView.findNavController()
                    .navigate(R.id.personal_to_edit, bundleOf("id_timeslot" to id))
                Snackbar.make(it, "Edit your timeslot here", 1500)
                    .show()
            }
        }
    }
}
