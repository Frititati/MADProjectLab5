package it.polito.timebanking.ui.user_profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentShowProfileBinding
import it.polito.timebanking.model.profile.ProfileViewModel
import it.polito.timebanking.model.skill.SkillData
import it.polito.timebanking.model.skill.toSkillData
import java.text.DecimalFormat

class ShowProfileFragment : Fragment() {
    private var _binding: FragmentShowProfileBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<ProfileViewModel>()
    private val skillsListAdapter = SkillsListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener { res ->
            if (res.exists()) {
                vm.get(FirebaseAuth.getInstance().currentUser!!.uid).observe(viewLifecycleOwner) {
                    binding.fullName.text = it.fullName
                    binding.nickName.text = it.nickName
                    binding.email.text = it.email
                    binding.age.text = String.format(resources.getString(R.string.age), it.age)
                    binding.location.text = it.location
                    binding.description.text = it.description
                    if (it.jobsRated > 0) {
                        val f = DecimalFormat("#.0")
                        Log.d("test","Score = ${it.score.toString().toDouble()} / ${it.jobsRated.toDouble()}")
                        binding.rating.text = f.format(it.score / it.jobsRated.toDouble()).toString()
                    }
                    Firebase.storage.getReferenceFromUrl(String.format(resources.getString(R.string.firebaseUserPic, FirebaseAuth.getInstance().currentUser!!.uid))).getBytes(1024 * 1024).addOnSuccessListener { pic ->
                        binding.userImage.setImageBitmap(BitmapFactory.decodeByteArray(pic, 0, pic.size))
                    }
                    binding.skillView.layoutManager = LinearLayoutManager(activity)
                    binding.skillView.adapter = skillsListAdapter
                    skillsListAdapter.setUserSkills(it.skills.map { l -> l.toString() })
                    binding.skillView.isNestedScrollingEnabled = false
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Unexpected error", Toast.LENGTH_LONG).show()
        }
        updateAllSkills()

        binding.buttonRate!!.setOnClickListener {
            findNavController().navigate(R.id.show_to_ratings)
            Snackbar.make(binding.root, "Tap on a rating to see more", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                skillsListAdapter.setEmptyLists()
                findNavController().navigate(
                    R.id.show_to_edit,
                )
                Snackbar.make(binding.root, "Edit your profile here", 1500).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateAllSkills() {
        FirebaseFirestore.getInstance().collection("skills").get().addOnSuccessListener { documents ->
            val map = mutableMapOf<String, SkillData>()
            for (document in documents) {
                map[document.id] = document.toSkillData()
            }
            skillsListAdapter.setAllSkills(map)
        }
    }


}