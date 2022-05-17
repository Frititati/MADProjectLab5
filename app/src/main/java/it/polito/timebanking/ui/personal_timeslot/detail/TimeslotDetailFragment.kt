package it.polito.timebanking.ui.personal_timeslot.detail

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import it.polito.timebanking.R
import it.polito.timebanking.model.timeslot.TimeslotViewModel
import it.polito.timebanking.databinding.FragmentTimeslotDetailBinding
import it.polito.timebanking.model.timeslot.*

class TimeslotDetailFragment : Fragment() {
    private val vm by viewModels<TimeslotViewModel>()
    private var _binding: FragmentTimeslotDetailBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeslotDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idTimeslot: String? = arguments?.getString("id_timeslot")
        vm.get(idTimeslot!!).observe(viewLifecycleOwner) {
            binding.Title.text = titleFormatter(it.title)
            binding.Description.text = descriptionFormatter(it.description)
            binding.Date.text = dateFormatter(it.date)
            binding.Duration.text = durationMinuteFormatter(resources, it.duration)
            binding.Location.text = locationFormatter(it.location)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_settings).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val idTimeslot: String? = arguments?.getString("id_timeslot")
                findNavController().navigate(
                    R.id.detail_to_edit,
                    bundleOf("id_timeslot" to idTimeslot)
                )
                Snackbar.make(binding.root, "Here you can edit your timeslot", 1000).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

