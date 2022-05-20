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
import it.polito.timebanking.databinding.FragmentEditSkillBinding
import it.polito.timebanking.model.profile.toUserProfileData
import it.polito.timebanking.model.timeslot.SkillData
import it.polito.timebanking.model.timeslot.toSkillData


class EditSkillFragment : Fragment() {
    private var _binding: FragmentEditSkillBinding? = null
    private val binding get() = _binding!!
    private var firestoreUser = FirebaseAuth.getInstance().currentUser
    private var editableSkillListAdapter = EditSkillAdapter()
    private val allSkills = mutableListOf<String>()
    private var newSkill = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSkillBinding.inflate(inflater, container, false)
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
        val dialog = AlertDialog.Builder(context)
        val dialogConfirm = AlertDialog.Builder(context)
        val dialogView = this.layoutInflater.inflate(R.layout.dialog_add_skill, null)
        val dialogConfirmView = this.layoutInflater.inflate(R.layout.dialog_generic,null)
        dialog.setTitle("Insert a skill")
        dialog.setView(dialogView)
        dialogConfirm.setTitle("Similar skill found. Are you sure you want to add a new one?")
        dialogConfirm.setView(dialogConfirmView)

        dialog.setPositiveButton("Done") { _, _ ->
            newSkill = dialogView.findViewById<EditText>(R.id.skillName).text.toString()
            if(allSkills.contains(newSkill)) {
                Toast.makeText(context, "$newSkill already exist!", Toast.LENGTH_LONG).show()
                updateAllSkills()
            }
            else if(allSkills.any { lockMatch(it, newSkill) >= 0.1 }){
                dialogConfirm.setPositiveButton("Yes"){_,_ ->
                createNewSkill()
                }
                dialogConfirm.setNegativeButton("No"){_,_ ->
                    updateAllSkills()
                }
                dialogConfirm.create().show()
            }
            else{
                createNewSkill()
            }
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            updateAllSkills()
        }
        dialog.setOnCancelListener {
            updateAllSkills()
        }
        dialog.create().show()

    }

    private fun createNewSkill() {
        if (newSkill != "") {
            FirebaseFirestore.getInstance().collection("skills")
                .add(
                    SkillData(newSkill)
                )
                .addOnSuccessListener {
                    newSkill = ""
                    updateAllSkills()
                    Snackbar.make(binding.root, "New Skill Created", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    newSkill = ""
                    Snackbar.make(binding.root, "Skill BAD", Snackbar.LENGTH_SHORT).show()
                }
        }
    }


    private fun lockMatch(s: String, t: String): Int {
        val totalWord = wordCount(s)
        val total = 100
        val perWord = total / totalWord
        var gotperw = 0
        if (s != t) {
            for (i in 1..totalWord) {
                gotperw = if (simpleMatch(splitString(s, i), t) == 1) {
                    perWord * (total - 10) / total + gotperw
                } else if (frontFullMatch(splitString(s, i), t) == 1) {
                    perWord * (total - 20) / total + gotperw
                } else if (anywhereMatch(splitString(s, i), t) == 1) {
                    perWord * (total - 30) / total + gotperw
                } else {
                    perWord * smartMatch(splitString(s, i), t) / total + gotperw
                }
            }
        } else {
            gotperw = 100
        }
        return gotperw
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
        val slen = s.size
        //number of 3 combinations per word//
        val combs = slen - 3 + 1
        //percentage per combination of 3 characters//
        var ppc = 0
        if (slen >= 3) {
            ppc = 100 / combs
        }
        //initialising an integer to store the total % this class genrate//
        var x = 0
        //declaring a temporary new source char array
        val ns = CharArray(3)
        //check if source char array has more then 3 characters//
        if (slen >= 3) {
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
                        // x=1 if any character matches
                        x = 1
                    } else {
                        // if x=0 mean an character do not matches and loop break out
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
        var temp: String
        temp = s
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