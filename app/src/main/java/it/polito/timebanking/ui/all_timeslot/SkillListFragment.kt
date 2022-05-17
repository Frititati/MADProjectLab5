package it.polito.timebanking.ui.all_timeslot

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentSkillListBinding
import it.polito.timebanking.model.timeslot.SkillData
import it.polito.timebanking.model.timeslot.toSkillData


class SkillListFragment : Fragment() {

    private var _binding: FragmentSkillListBinding? = null
    private val binding get() = _binding!!
    private var skillListAdapter = SkillListAdapter()
    private var counter = 0
    private var _firebase: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSkillListBinding.inflate(inflater, container, false)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val dialog = AlertDialog.Builder(context,R.style.AlertDialogStyle)
                    val dialogView = layoutInflater.inflate(R.layout.dialog_exit, null)
                    dialog.setTitle("Are you sure you want to quit?")
                    dialog.setView(dialogView)
                    dialog.setPositiveButton("Yes") { _, _ ->
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_HOME)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }

                    dialog.setNegativeButton("NO") { _, _ ->
                    }
                    dialog.create().show()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.skillListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.skillListRecycler.adapter = skillListAdapter
        updateAllSkills()

        binding.updateButton.setOnClickListener {
            updateAllSkills()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateAllSkills() {
        val map: MutableList<Pair<String, SkillData>> = mutableListOf()
        _firebase.collection("skills").get()
            .addOnSuccessListener { documents ->
                binding.nothingToShow.isVisible = documents.size() == 0
                for (document in documents) {
                    map.add(Pair(document.id, document.toSkillData()))
                }
                skillListAdapter.setSkills(map)
            }
    }

}