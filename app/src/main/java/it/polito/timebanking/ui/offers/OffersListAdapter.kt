package it.polito.timebanking.ui.offers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.polito.timebanking.R
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.*

class OffersListAdapter(private val mode: String) :
    RecyclerView.Adapter<OffersListAdapter.OfferListViewHolder>(),
    Filterable {
    private var timeslots: MutableList<Pair<String, TimeslotData>> = mutableListOf()
    private var timeslotsFull: MutableList<Pair<String, TimeslotData>> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferListViewHolder {
        return OfferListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.widget_offer, parent, false), mode
        )
    }

    override fun onBindViewHolder(holder: OfferListViewHolder, position: Int) {
        holder.bind(timeslots[position].first, timeslots[position].second)
    }

    override fun getItemCount() = timeslots.size

    @SuppressLint("NotifyDataSetChanged")
    fun setTimeslots(inTimeslots: MutableList<Pair<String, TimeslotData>>) {
        timeslots = inTimeslots
        timeslotsFull.addAll(timeslots)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addTimeslots(inTimeslot: Pair<String, TimeslotData>) {
        timeslots.add(inTimeslot)
        timeslotsFull.add(inTimeslot)
        notifyDataSetChanged()
    }

    class OfferListViewHolder(v: View, private val mode: String) : RecyclerView.ViewHolder(v) {
        private val rootView = v
        val title: TextView = v.findViewById(R.id.title)
        private val location: TextView = v.findViewById(R.id.location)
        private val date: TextView = v.findViewById(R.id.date)
        private val button: Button = v.findViewById(R.id.view_button)

        fun bind(id: String, timeslot: TimeslotData) {
            title.text = titleFormatter(timeslot.title)
            location.text = locationFormatter(timeslot.location)
            date.text = dateFormatter(timeslot.date)

            button.setOnClickListener {
                if (mode == "Watch")
                    rootView.findNavController()
                        .navigate(
                            R.id.offers_to_offer,
                            bundleOf("id_timeslot" to id, "id_user" to timeslot.ownedBy)
                        )
                Snackbar.make(it, "Here you can view details about ${title.text}", 1500)
                    .show()
                if (mode == "Fav")
                    rootView.findNavController()
                        .navigate(
                            R.id.favoritesToDetail,
                            bundleOf("id_timeslot" to id, "id_user" to timeslot.ownedBy)
                        )
                Snackbar.make(it, "Here you can view details about ${title.text}", 1500)
                    .show()
            }
        }
    }

    override fun getFilter(): Filter {
        return exampleFilter
    }

    private val exampleFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            var filteredList: MutableList<Pair<String, TimeslotData>> = mutableListOf()
            if (constraint.isEmpty())
                filteredList.addAll(timeslotsFull)
            else {
                val pattern = constraint.toString().lowercase().trim()
                filteredList =
                    timeslotsFull.filter { it.second.title.lowercase().contains(pattern) }
                        .toMutableList()
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(p0: CharSequence?, results: FilterResults) {
            timeslots.clear()
            timeslots.addAll(results.values as MutableList<Pair<String, TimeslotData>>)
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortByDate(descending: Boolean) {
        timeslots.clear().run { timeslots.addAll(timeslotsFull.sortedBy { it.second.date }) }
        if (descending)
            timeslots.reverse()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortByDuration(descending: Boolean) {
        timeslots.clear().run { timeslots.addAll(timeslotsFull.sortedBy { it.second.duration }) }
        if (descending)
            timeslots.reverse()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterByDuration(duration: Int): Int {
        timeslots.clear()
            .run { timeslots.addAll(timeslotsFull.filter { it.second.duration >= duration }) }
        notifyDataSetChanged()
        return timeslots.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectDate(date: Long): Int {
        timeslots.clear().run { timeslots.addAll(timeslotsFull.filter { it.second.date == date }) }
        notifyDataSetChanged()
        return timeslots.size
    }
}
