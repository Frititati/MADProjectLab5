package it.polito.timebanking.ui.personal_timeslot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.databinding.FragmentTimeslotListBinding
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.TimeslotViewModel


class TimeSlotListFragment : Fragment() {
    private var _binding: FragmentTimeslotListBinding? = null
    private val binding get() = _binding!!
    private var counter = 0
    private val vm by viewModels<TimeslotViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeslotListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val timeslotListAdapter = TimeslotListAdapter()
        binding.timeslotRecycler.layoutManager = LinearLayoutManager(activity)
        binding.timeslotRecycler.adapter = timeslotListAdapter

        vm.getUserTimeslots(FirebaseAuth.getInstance().currentUser!!.uid)
            .observe(viewLifecycleOwner) {
                timeslotListAdapter.setTimeslots(it.sortedByDescending { t -> t.second.date }
                    .toMutableList())
                counter = it.size
                binding.nothingToShow.isVisible = counter == 0
            }

        binding.buttonAdd.setOnClickListener {
            addEmptyTimeslot()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addEmptyTimeslot() {
        FirebaseFirestore.getInstance().collection("timeslots")
            .add(
                TimeslotData(
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    "",
                    "",
                    0,
                    0,
                    "",
                    FirebaseAuth.getInstance().currentUser!!.uid,
                )
            )
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Time Slot Created", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Time Slot BAD", Snackbar.LENGTH_SHORT).show()
            }
    }
}
