package it.polito.timebanking.ui.personal_timeslot

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentTimeslotEditBinding
import it.polito.timebanking.model.profile.toUserProfileData
import it.polito.timebanking.model.timeslot.TimeslotViewModel
import it.polito.timebanking.model.timeslot.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.concurrent.thread

class EditTimeslotFragment : Fragment() {
    private var _binding: FragmentTimeslotEditBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<TimeslotViewModel>()
    private var editableSkillListAdapter = EditTimeslotSkillAdapter()
    private var initialDate = ""
    private var idTimeslot: String = ""
    private var toUpdate: Boolean = true

    private var dateMilli: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )

        _binding = FragmentTimeslotEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.skillListRecycler!!.layoutManager = LinearLayoutManager(activity)
        binding.skillListRecycler!!.adapter = editableSkillListAdapter

        idTimeslot = requireArguments().getString("id_timeslot")!!
        vm.get(idTimeslot).observe(viewLifecycleOwner) {
            binding.editTitle.hint = titleFormatter(it.title)
            binding.editDescription.hint = descriptionFormatter(it.description)
            binding.editDate.hint = dateFormatter(it.date)
            initialDate = dateFormatter(it.date)
            binding.editDuration.hint = durationFormatter(it.duration).toString() + " minutes"
            binding.editLocation.hint = locationFormatter(it.location)

            editableSkillListAdapter.setTimeslotSkills(
                idTimeslot,
                it.skills.map { l -> l.toString() })

            if (it.available) {
                binding.activateButton!!.visibility = View.GONE
                binding.deactivateButton!!.visibility = View.VISIBLE
            } else {
                binding.activateButton!!.visibility = View.VISIBLE
                binding.deactivateButton!!.visibility = View.GONE
            }
        }

        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().uid!!).get()
            .addOnSuccessListener { userIt ->
                val user = userIt.toUserProfileData()
                editableSkillListAdapter.setAvailableSkills(user.skills.map { it.toString() })
            }

        val cal = Calendar.getInstance()
        binding.editDateButton.setOnClickListener {
            val dpd = DatePickerDialog(requireContext(), { _, y, m, d ->
                val date =
                    LocalDateTime.parse("$y-${"%02d".format(m + 1)}-${"%02d".format(d)}T00:00:00")
                dateMilli = date.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
                binding.editDate.text = dateFormatter(dateMilli)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000
            dpd.show()
        }

        binding.activateButton!!.setOnClickListener {
            val y: Int
            val m: Int
            val d: Int
            if (binding.editDate.text.isEmpty()) {
                y = getYear(initialDate)
                m = getMonth(initialDate)
                d = getDay(initialDate)
            } else {
                y = getYear(binding.editDate.text.toString())
                m = getMonth(binding.editDate.text.toString())
                d = getDay(binding.editDate.text.toString())
            }
            val date = LocalDateTime.parse("$y-${"%02d".format(m)}-${"%02d".format(d)}T00:00:00")
            dateMilli = date.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
            binding.editDate.hint = dateFormatter(dateMilli)
            if (dateMilli + 86399999 >= System.currentTimeMillis()) {
                FirebaseFirestore.getInstance().collection("timeslots").document(idTimeslot)
                    .update("available", true).addOnSuccessListener {
                        Snackbar.make(binding.root, "Timeslot now active", 1500).show()
                    }
            }
            else{
                Toast.makeText(context,"Date should be today or after",Toast.LENGTH_SHORT).show()
            }
        }

        binding.deactivateButton!!.setOnClickListener {
            FirebaseFirestore.getInstance().collection("timeslots").document(idTimeslot)
                .update("available", false).addOnSuccessListener {
                    Snackbar.make(binding.root, "Timeslot now not active", 1500).show()
                }
        }

        binding.deleteButton.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            val dialogView = this.layoutInflater.inflate(R.layout.dialog_generic, null)
            dialog.setTitle(
                String.format(
                    resources.getString(R.string.delete_timeslot_confirm),
                )
            )
            dialog.setView(dialogView)

            dialog.setPositiveButton("Yes") { _, _ ->
                toUpdate = false
                vm.delete(idTimeslot)
                vm.deleteUserTimeslot(idTimeslot)
                findNavController().navigate(R.id.edit_to_personal)
                Snackbar.make(binding.root, "Timeslot deleted", 1500).show()
            }
            dialog.setNegativeButton("No") { _, _ ->
            }
            dialog.create().show()
        }
    }

    private fun getYear(text: String): Int {
        return text.substring(6).toInt()
    }

    private fun getMonth(text: String): Int {
        return text.substring(3, 5).toInt()
    }

    private fun getDay(text: String): Int {
        return text.substring(0, 2).toInt()
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
                    binding.editLocation.text.toString()
                )
                Snackbar.make(binding.root, "Updated Timeslot", 1500).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    private fun updateAllSkills() {
//        FirebaseFirestore.getInstance().collection("users")
//            .document(FirebaseAuth.getInstance().uid!!).get()
//            .addOnSuccessListener { r ->
//                if (r != null) {
//                    userSkills = r.toUserProfileData().skills.map { it.toString() }
//                }
//            }
//    }
}

