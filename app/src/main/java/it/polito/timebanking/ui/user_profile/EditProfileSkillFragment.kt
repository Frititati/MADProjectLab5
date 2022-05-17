package it.polito.timebanking.ui.user_profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentEditProfileSkillBinding
import it.polito.timebanking.model.profile.toUserProfileData
import it.polito.timebanking.model.timeslot.SkillData
import it.polito.timebanking.model.timeslot.toSkillData


class EditProfileSkillFragment : Fragment() {
    private var _binding: FragmentEditProfileSkillBinding? = null
    private val binding get() = _binding!!
    private var firestoreUser = FirebaseAuth.getInstance().currentUser
    private var editableSkillListAdapter = EditableSkillListAdapter()
    private val allSkills = mutableListOf<String>()
    private var newSkillname = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileSkillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.skillListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.skillListRecycler.adapter = editableSkillListAdapter
        updateAllSkills()

        binding.buttonAdd.setOnClickListener {
            createDialog()
        }
    }

    private fun updateAllSkills() {
        FirebaseFirestore.getInstance().collection("skills").get()
            .addOnSuccessListener { documents ->
                val map: MutableList<Pair<String, SkillData>> = mutableListOf()
                for (document in documents) {
                    map.add(Pair(document.id, document.toSkillData()))
                    allSkills.add(document.get("title").toString())
                }
                editableSkillListAdapter.setAllSkills(map)
            }

        FirebaseFirestore.getInstance().collection("users").document(firestoreUser!!.uid).get()
            .addOnSuccessListener { r ->
                if (r != null) {
                    editableSkillListAdapter.setUserSkills(r.toUserProfileData().skills)
                }
            }
    }


    private fun createDialog() {
        editableSkillListAdapter.setEmptyLists()
        val dialog = AlertDialog.Builder(context,R.style.AlertDialogStyle)
        val dialogView = this.layoutInflater.inflate(R.layout.dialog_add_skill, null)
        val skillNameText = dialogView.findViewById<EditText>(R.id.skillName)
        dialog.setTitle("Insert a skill")
        dialog.setView(dialogView)

        dialog.setPositiveButton("Done") { _, _ ->
            newSkillname = skillNameText.text.toString()
            skillNameText.setText("")
            if(allSkills.contains(newSkillname)) {
                Toast.makeText(context, "$newSkillname already exist!", Toast.LENGTH_LONG).show()
                updateAllSkills()
            }
            else
                createNewSkill()
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            skillNameText.setText("")
            updateAllSkills()
        }
        dialog.setOnCancelListener {
            skillNameText.setText("")
            updateAllSkills()
        }
        dialog.create().show()

    }

    private fun createNewSkill() {
        if (newSkillname != "") {
            FirebaseFirestore.getInstance().collection("skills")
                .add(
                    SkillData(newSkillname)
                )
                .addOnSuccessListener {
                    newSkillname = ""
                    updateAllSkills()
                    Snackbar.make(binding.root, "New Skill Created", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    newSkillname = ""
                    Snackbar.make(binding.root, "Skill BAD", Snackbar.LENGTH_SHORT).show()
                }
        }
    }
}