package it.polito.timebanking.ui.personal_timeslot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentTimeslotListBinding
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.TimeslotViewModel

class TimeslotListFragment : Fragment() {
    private var _binding: FragmentTimeslotListBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<TimeslotViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(
            DrawerLayout.LOCK_MODE_UNLOCKED
        )
        _binding = FragmentTimeslotListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val timeslotListAdapter = TimeslotListAdapter()
        binding.timeslotRecycler.layoutManager = LinearLayoutManager(activity)
        binding.timeslotRecycler.adapter = timeslotListAdapter
        binding.nothingToShow.text = resources.getString(R.string.no_timeslots)

        vm.getUserTimeslots().observe(viewLifecycleOwner) {
            timeslotListAdapter.setTimeslots(it.sortedBy { t -> t.second.title }
                .toMutableList())
            binding.nothingToShow.isVisible = it.isEmpty()
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
                    available = false,
                    listOf<String>()
                )
            )
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid).update(
                        "timeslots", FieldValue.arrayUnion(it.id)
                    )
                Snackbar.make(binding.root, "Time Slot Created", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Time Slot BAD", Snackbar.LENGTH_SHORT).show()
            }
    }
}
