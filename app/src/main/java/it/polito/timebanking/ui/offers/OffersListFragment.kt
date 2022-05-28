package it.polito.timebanking.ui.offers

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentOffersBinding
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.TimeslotViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class OffersListFragment : Fragment() {

    private var _binding: FragmentOffersBinding? = null
    private val offersListAdapter = OffersListAdapter()
    private val binding get() = _binding!!
    private val vmTimeslot by viewModels<TimeslotViewModel>()
    private lateinit var listener: NavBarUpdater

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )

        listener = context as NavBarUpdater
        listener.setTitleWithSkill("Offers for" + " " + requireArguments().getString("offerName"))
        _binding = FragmentOffersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedSkill: String = requireArguments().getString("skill_select")!!
        binding.timeslotRecycler.layoutManager = LinearLayoutManager(activity)
        binding.timeslotRecycler.adapter = offersListAdapter
        binding.buttonAdd.isVisible = false

        vmTimeslot.getTimeslotsForSkill(selectedSkill).observe(viewLifecycleOwner) {

            offersListAdapter.setTimeslots(it as MutableList<Pair<String, TimeslotData>>)
            binding.nothingToShow.text = String.format(
                resources.getString(
                    R.string.no_offers_found,
                    requireArguments().getString("offerName")
                )
            )
            binding.nothingToShow.isVisible = it.isEmpty()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        menu.findItem(R.id.action_sortOffers).isVisible = true
        menu.findItem(R.id.action_sortOffers).subMenu.clear()
        menu.findItem(R.id.action_sortOffers).subMenu.add(0, 1, 0, "Most recent")
        menu.findItem(R.id.action_sortOffers).subMenu.add(0, 2, 0, "Least recent")
        menu.findItem(R.id.action_sortOffers).subMenu.add(0, 3, 0, "Longer time")
        menu.findItem(R.id.action_sortOffers).subMenu.add(0, 4, 0, "Shorter time")
        menu.findItem(R.id.action_filterOffers).isVisible = true
        menu.findItem(R.id.action_filterOffers).subMenu.clear()
        menu.findItem(R.id.action_filterOffers).subMenu.add(0, 5, 0, "Duration")
        menu.findItem(R.id.action_filterOffers).subMenu.add(0, 6, 0, "Date")
        /*if (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
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
         */
        menu.findItem(R.id.action_search).isVisible = true
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                offersListAdapter.filter.filter(newText)
                return false
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                binding.nothingToShow.isVisible = false
                offersListAdapter.sortByDate(true)
            }
            2 -> {
                binding.nothingToShow.isVisible = false
                offersListAdapter.sortByDate(false)
            }
            3 -> {
                binding.nothingToShow.isVisible = false
                offersListAdapter.sortByDuration(true)
            }
            4 -> {
                binding.nothingToShow.isVisible = false
                offersListAdapter.sortByDuration(false)
            }
            5 -> {
                binding.nothingToShow.isVisible = false
                numberPickerCustom()
            }
            6 -> {
                binding.nothingToShow.isVisible = false
                datePickerCustom()
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun numberPickerCustom() {
        val dialog = AlertDialog.Builder(context)
        val dialogView = this.layoutInflater.inflate(R.layout.dialog_filter, null)
        dialog.setTitle("Choose minimum duration")
        dialog.setView(dialogView)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.dialog_number_picker)
        numberPicker.minValue = 1
        numberPicker.maxValue = 240
        numberPicker.wrapSelectorWheel = false
        dialog.setPositiveButton("Done") { _, _ ->
            val n = offersListAdapter.filterByDuration(numberPicker.value)
            if (n == 0) {
                binding.nothingToShow.text = String.format(
                    resources.getString(R.string.no_offers_duration),
                    numberPicker.value
                )
            }
        }
        dialog.setNegativeButton("Cancel") { _, _ -> }
        dialog.create().show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun datePickerCustom() {
        val cal = Calendar.getInstance()
        val dpd = DatePickerDialog(requireContext(), { _, y, m, d ->
            val n = offersListAdapter.selectDate(
                LocalDateTime.parse(
                    "$y-${"%02d".format(m + 1)}-${"%02d".format(d)}T00:00:00"
                ).atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
            )
            if (n == 0) {
                binding.nothingToShow.isVisible = true
                binding.nothingToShow.text = String.format(
                    resources.getString(R.string.no_offers_date),
                    "%02d".format(d),
                    "%02d".format(m + 1),
                    y.toString()
                )

            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dpd.datePicker.minDate = System.currentTimeMillis()
        dpd.show()
    }
}