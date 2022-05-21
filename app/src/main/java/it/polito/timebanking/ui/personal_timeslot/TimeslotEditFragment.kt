package it.polito.timebanking.ui.personal_timeslot

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentTimeslotEditBinding
import it.polito.timebanking.model.profile.toUserProfileData
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.TimeslotViewModel
import it.polito.timebanking.model.timeslot.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.concurrent.thread


class TimeslotEditFragment : Fragment() {
    private var _binding: FragmentTimeslotEditBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<TimeslotViewModel>()
    private var firestoreUser = FirebaseAuth.getInstance().currentUser
    private var _firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var idTimeslot: String = ""
    private var toUpdate: Boolean = true
    private lateinit var currentTimeSlot: TimeslotData

    private lateinit var userSkills: List<String>

    private var dateMilli: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        _binding = FragmentTimeslotEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateAllSkills()

        idTimeslot = requireArguments().getString("id_timeslot")!!
        vm.get(idTimeslot).observe(viewLifecycleOwner) {
            currentTimeSlot = it
            binding.editTitle.hint = titleFormatter(it.title)
            binding.editDescription.hint = descriptionFormatter(it.description)
            binding.editDate.hint = dateFormatter(it.date)
            binding.editDuration.hint = durationFormatter(it.duration).toString() + " minutes"
            binding.editLocation.hint = locationFormatter(it.location)
        }
        val cal = Calendar.getInstance()
        binding.editDateButton.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val date =
                    LocalDateTime.parse("$y-${"%02d".format(m + 1)}-${"%02d".format(d)}T00:00:00")
                dateMilli = date.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
                binding.editDate.text = dateFormatter(dateMilli)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        binding.deleteButton.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            val dialogView = this.layoutInflater.inflate(R.layout.dialog_generic,null)
            dialog.setTitle(String.format(resources.getString(R.string.delete_timeslot_confirm),currentTimeSlot.title))
            dialog.setView(dialogView)

            dialog.setPositiveButton("Yes"){_,_ ->
                vm.delete(idTimeslot)
                toUpdate = false
                Snackbar.make(binding.root, "Timeslot deleted", 1500).setAction("Undo") {
                    addNonEmptyTimeslot(currentTimeSlot)
                }.show()
                findNavController().navigate(R.id.edit_to_list)
            }
            dialog.setNegativeButton("No") {_,_ ->
            }
            dialog.create().show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (toUpdate) {
            thread {
                vm.update(
                    idTimeslot,
                    binding.editTitle.text.toString(),
                    binding.editDescription.text.toString(),
                    dateMilli,
                    binding.editDuration.text.toString(),
                    binding.editLocation.text.toString(),
                    userSkills
                )
                Snackbar.make(binding.root, "Updated Timeslot", 1500).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addNonEmptyTimeslot(t: TimeslotData) {
        FirebaseFirestore.getInstance().collection("timeslots")
            .add(
                TimeslotData(
                    t.createdAt,
                    t.editedAt,
                    t.title,
                    t.description,
                    t.date,
                    t.duration,
                    t.location,
                    t.ownedBy,
                )
            )
    }

    private fun updateAllSkills() {
        _firestore.collection("users").document(firestoreUser!!.uid).get()
            .addOnSuccessListener { r ->
                if (r != null) {
                    userSkills = r.toUserProfileData().skills
                }
            }
    }
}

