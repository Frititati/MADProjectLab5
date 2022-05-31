package it.polito.timebanking.ui.user_profile

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentEditSkillBinding
import it.polito.timebanking.model.profile.toUserProfileData
import it.polito.timebanking.model.skill.SkillViewModel
import it.polito.timebanking.model.skill.SkillData


class EditSkillFragment : Fragment() {
    private var _binding: FragmentEditSkillBinding? = null
    private val binding get() = _binding!!
    private val firebaseUserID = FirebaseAuth.getInstance().uid!!
    private val vm by viewModels<SkillViewModel>()
    private var editableSkillListAdapter = EditSkillAdapter()
    private var allSkills = mutableListOf<SkillData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSkillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAdd.useCompatPadding = false
        binding.skillListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.skillListRecycler.adapter = editableSkillListAdapter
        vm.get().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                FirebaseFirestore.getInstance().collection("users").document(firebaseUserID).get().addOnSuccessListener { r ->
                    if (r != null) {
                        val myList = r.toUserProfileData().skills
                        editableSkillListAdapter.setUserSkills(myList.map { t -> t.toString() })
                    }
                    allSkills = it.map { t -> t.second }.toMutableList()
                    binding.nothingToShow.isVisible = it.isEmpty()
                    editableSkillListAdapter.setSkills(it.sortedBy { t -> t.second.title.lowercase() } as MutableList<Pair<String, SkillData>>)
                }.addOnFailureListener { e -> Log.w("warn", "Error with users $e") }
            }
        }

        binding.buttonAdd.setOnClickListener {
            createDialog()
        }
    }

    override fun onPause() {
        super.onPause()
        Snackbar.make(binding.root, "Skills updated", Snackbar.LENGTH_SHORT).show()

    }

    /*private fun updateAllSkills() {
        FirebaseFirestore.getInstance().collection("skills").get().addOnSuccessListener { documents ->
            val map = mutableListOf<Pair<String, SkillData>>()
            for (document in documents) {
                map.add(Pair(document.id, document.toSkillData()))
                allSkills.add(document.toSkillData())
            }
            editableSkillListAdapter.setSkills(map.sortedBy { it.second.title.lowercase() })
        }

        FirebaseFirestore.getInstance().collection("users").document(firebaseUserID).get().addOnSuccessListener { r ->
            if (r != null) {
                val myList = r.toUserProfileData().skills
                editableSkillListAdapter.setUserSkills(myList.map { it.toString() })
            }
        }
    }*/

    private fun createDialog() {
//        editableSkillListAdapter.setEmptyLists()
        val dialog = AlertDialog.Builder(context)
        val dialogConfirm = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_skill, null)
        val dialogConfirmView = layoutInflater.inflate(R.layout.dialog_generic, null)
        dialog.setTitle("Write your skill")
        dialog.setView(dialogView)
        dialogConfirm.setTitle("Similar skill found. Are you sure you want to add a new one?")
        dialogConfirm.setView(dialogConfirmView)

        dialog.setPositiveButton("Done") { _, _ ->
            val newSkill = dialogView.findViewById<EditText>(R.id.skillName).text.trim().toString().uppercase()
            if (newSkill.isEmpty()) {
                Toast.makeText(context, "Cannot create empty skill", Toast.LENGTH_SHORT).show()
//                updateAllSkills()
            } else if (isUnique(allSkills, newSkill)) {
                Toast.makeText(context, "$newSkill already exist!", Toast.LENGTH_SHORT).show()
//                updateAllSkills()
            } else if (allSkills.any { lockMatch(it.title, newSkill) >= 0.1 }) {
                dialogConfirm.setPositiveButton("Yes") { _, _ ->
                    createNewSkill(newSkill)
                }
                dialogConfirm.setNegativeButton("No") { _, _ ->
//                    updateAllSkills()
                }
                dialogConfirm.create().show()
            } else {
                createNewSkill(newSkill)
            }
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
//            updateAllSkills()
        }
        dialog.setOnCancelListener {
//            updateAllSkills()
        }
        dialog.create().show()

    }

    private fun createNewSkill(newSkill: String) {
        FirebaseFirestore.getInstance().collection("skills").add(SkillData(newSkill)).addOnSuccessListener {
//            updateAllSkills()
            Snackbar.make(binding.root, "New Skill Created", Snackbar.LENGTH_SHORT).show()
        }.addOnFailureListener { e -> Log.w("warn", "Error with skills $e") }
    }

    private fun isUnique(list: List<SkillData>, skill: String): Boolean {
        return list.any { it.title.lowercase() == skill.lowercase().trim() }
    }

    private fun lockMatch(s: String, t: String): Int {
        val totalWord = wordCount(s)
        val total = 100
        val perWord = total / totalWord
        var gotW = 0
        if (s != t) {
            for (i in 1..totalWord) {
                gotW = if (simpleMatch(splitString(s, i), t) == 1) {
                    perWord * (total - 10) / total + gotW
                } else if (frontFullMatch(splitString(s, i), t) == 1) {
                    perWord * (total - 20) / total + gotW
                } else if (anywhereMatch(splitString(s, i), t) == 1) {
                    perWord * (total - 30) / total + gotW
                } else {
                    perWord * smartMatch(splitString(s, i), t) / total + gotW
                }
            }
        } else {
            gotW = 100
        }
        return gotW
    }

    private fun anywhereMatch(s: String?, t: String): Int {
        var x = 0
        if (t.contains(s!!)) {
            x = 1
        }
        return x
    }

    private fun frontFullMatch(s: String?, t: String): Int {
        var x = 0
        var tempt: String?
        val len = s!!.length

        for (i in 1..wordCount(t)) {
            tempt = splitString(t, i)
            if (tempt!!.length >= s.length) {
                tempt = tempt.substring(0, len)
                if (s.contains(tempt)) {
                    x = 1
                    break
                }
            }
        }
        if (len == 0) {
            x = 0
        }
        return x
    }

    private fun simpleMatch(s: String?, t: String): Int {
        var x = 0
        var tempt: String?
        val len = s!!.length

        for (i in 1..wordCount(t)) {
            tempt = splitString(t, i)
            if (tempt!!.length == s.length) {
                if (s.contains(tempt)) {
                    x = 1
                    break
                }
            }
        }
        if (len == 0) {
            x = 0
        }
        return x
    }

    private fun smartMatch(ts: String?, tt: String): Int {
        val s: CharArray = ts!!.toCharArray()
        val t: CharArray = tt.toCharArray()
        val sLen = s.size
        val combs = sLen - 3 + 1
        var ppc = 0
        if (sLen >= 3) {
            ppc = 100 / combs
        }
        var x = 0
        val ns = CharArray(3)
        if (sLen >= 3) {
            for (i in 0 until combs) {
                for (j in 0..2) {
                    ns[j] = s[j + i]
                }
                if (crossFullMatch(ns, t) == 1) {
                    x += 1
                }
            }
        }
        x *= ppc
        return x
    }

    private fun crossFullMatch(s: CharArray, t: CharArray): Int {
        val z = t.size - s.size
        var x = 0
        if (s.size > t.size) {
            return x
        } else {
            for (i in 0..z) {
                for (j in s.indices) {
                    if (s[j] == t[j + i]) {
                        x = 1
                    } else {
                        x = 0
                        break
                    }
                }
                if (x == 1) {
                    break
                }
            }
        }
        return x
    }

    private fun splitString(s: String, n: Int): String? {
        var index: Int
        var temp = s
        var temp2: String? = null
        val temp3 = 0
        for (i in 0 until n) {
            val endIndex = temp.length
            index = temp.indexOf(" ")
            if (index < 0) {
                index = endIndex
            }
            temp2 = temp.substring(temp3, index)
            temp = temp.substring(index, endIndex)
            temp = temp.trim { it <= ' ' }
        }
        return temp2
    }

    private fun wordCount(s: String): Int {
        var str = s
        var x = 1
        var c: Int
        str = str.trim { it <= ' ' }
        if (str.isEmpty()) {
            x = 0
        } else {
            if (str.contains(" ")) {
                while (true) {
                    x++
                    c = str.indexOf(" ")
                    str = str.substring(c)
                    str = str.trim { it <= ' ' }
                    if (!str.contains(" ")) {
                        break
                    }
                }
            }
        }
        return x
    }
}