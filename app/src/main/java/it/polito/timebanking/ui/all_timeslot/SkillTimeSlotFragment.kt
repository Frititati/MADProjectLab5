package it.polito.timebanking.ui.all_timeslot

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentSkillTimeSlotBinding
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.dateFormatter
import it.polito.timebanking.model.timeslot.toTimeslotData
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class SkillTimeSlotFragment : Fragment() {

    private var _binding: FragmentSkillTimeSlotBinding? = null
    private var counter = 0
    private val timeslotListAdapter = TimeslotListAdapter()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkillTimeSlotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedSkill: String = arguments?.getString("skill_select")!!
        val usersAvailable = mutableListOf<String>()
        val timeslotsAdvertised: MutableList<Pair<String, TimeslotData>> = mutableListOf()
        binding.timeslotRecycler.layoutManager = LinearLayoutManager(activity)
        binding.timeslotRecycler.adapter = timeslotListAdapter
        binding.buttonAdd.isVisible = false

        FirebaseFirestore.getInstance().collection("users").get().addOnSuccessListener { userList ->
            for (user in userList) {
                val list: List<String> = user.get("skills") as List<String>
                if (list.contains(selectedSkill)) {
                    usersAvailable.add(user.id)
                }
            }
            FirebaseFirestore.getInstance().collection("timeslots").get().addOnSuccessListener {
                for (t in it) {
                    if (usersAvailable.contains(t.get("ownedBy")))
                        timeslotsAdvertised.add(Pair(t.id, t.toTimeslotData()))
                }
                timeslotListAdapter.setTimeslots(timeslotsAdvertised)
                counter = timeslotsAdvertised.size
                binding.nothingToShow.isVisible = counter == 0
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        menu.findItem(R.id.action_sort).isVisible = true
        if (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            for ((i, s) in listOf(
                "Sort by most recent",
                "Sort by least recent",
                "Sort by longer time",
                "Sort by shorter time",
                "Filter by duration",
                "Filter by date"
            ).withIndex()) {
                val colored = SpannableString(s)
                colored.setSpan(ForegroundColorSpan(Color.WHITE), 0, colored.length, 0)
                menu.findItem(R.id.action_sort).subMenu.add(0, i + 1, 0, colored)
            }
        } else {
            menu.findItem(R.id.action_sort).subMenu.add(0, 1, 0, "Sort by most recent")
            menu.findItem(R.id.action_sort).subMenu.add(0, 2, 0, "Sort by least recent")
            menu.findItem(R.id.action_sort).subMenu.add(0, 3, 0, "Sort by longer time")
            menu.findItem(R.id.action_sort).subMenu.add(0, 4, 0, "Sort by shorter time")
            menu.findItem(R.id.action_sort).subMenu.add(0, 5, 0, "Filter by duration")
            menu.findItem(R.id.action_sort).subMenu.add(0, 6, 0, "Filter by date")
        }
        menu.findItem(R.id.action_search).isVisible = true
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                timeslotListAdapter.filter.filter(newText)
                return false
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> timeslotListAdapter.sortByDate(true)
            2 -> timeslotListAdapter.sortByDate(false)
            3 -> timeslotListAdapter.sortByDuration(true)
            4 -> timeslotListAdapter.sortByDuration(false)
            5 -> {
                numberPickerCustom()
            }
            6 -> {
                datePickerCustom()
            }
            else -> binding.nothingToShow.isVisible = false
        }
        return super.onContextItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun numberPickerCustom() {
        val d = AlertDialog.Builder(context,R.style.AlertDialogStyle)
        val dialogView = this.layoutInflater.inflate(R.layout.dialog_filter_by_date, null)
        d.setTitle("Choose minimum duration")
        d.setView(dialogView)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.dialog_number_picker)
        numberPicker.minValue = 1
        numberPicker.maxValue = 240
        numberPicker.wrapSelectorWheel = false
        d.setPositiveButton("Done") { _, _ ->
            val n = timeslotListAdapter.filterByDuration(numberPicker.value)
            if(n == 0) {
                binding.nothingToShow.isVisible = true
                binding.nothingToShow.text = "No timeslots lasting ${numberPicker.value} minutes were found "
            }
        }
        d.setNegativeButton("Cancel") { _, _ -> }
        d.create().show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun datePickerCustom() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val n = timeslotListAdapter.selectDate(LocalDateTime.parse("$y-${"%02d".format(m + 1)}-${"%02d".format(d)}T00:00:00").atOffset(ZoneOffset.UTC).toInstant().toEpochMilli())
            if (n == 0) {
                binding.nothingToShow.isVisible = true
                binding.nothingToShow.text = "No timeslots with selected date were found"
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            .show()
    }
}