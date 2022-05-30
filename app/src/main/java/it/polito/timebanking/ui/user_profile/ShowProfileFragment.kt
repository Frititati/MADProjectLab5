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
import it.polito.timebanking.model.profile.*
import it.polito.timebanking.model.skill.SkillData
import it.polito.timebanking.model.skill.toSkillData
import java.text.DecimalFormat

class ShowProfileFragment : Fragment() {
    private var _binding: FragmentShowProfileBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<ProfileViewModel>()
    private val firebaseUserID = FirebaseAuth.getInstance().uid!!
    private val skillsListAdapter = SkillsListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        FirebaseFirestore.getInstance().collection("users").document(firebaseUserID).get().addOnSuccessListener {
            vm.get(firebaseUserID).observe(viewLifecycleOwner) {
                binding.fullName.text = fullNameFormatter(it.fullName, false)
                binding.nickName.text = nickNameFormatter(it.nickName, false)
                binding.email.text = emailFormatter(it.email, false)
                binding.age.text = ageFormatter(it.age, false)
                binding.location.text = locationFormatter(it.location, false)
                binding.description.text = descriptionFormatterProfile(it.description, false)
                if (it.jobsRatedAsProducer > 0) {
                    binding.ratingAsProducer.text = DecimalFormat("#.0").format(it.scoreAsProducer / it.jobsRatedAsProducer.toDouble()).toString()
                }
                if (it.jobsRatedAsConsumer > 0) {
                    binding.ratingAsConsumer.text = DecimalFormat("#.0").format(it.scoreAsConsumer / it.jobsRatedAsConsumer.toDouble()).toString()
                }
                Firebase.storage.getReferenceFromUrl(String.format(resources.getString(R.string.firebaseUserPic, firebaseUserID))).getBytes(1024 * 1024).addOnSuccessListener { pic ->
                    binding.userImage.setImageBitmap(BitmapFactory.decodeByteArray(pic, 0, pic.size))
                }
                binding.skillView.layoutManager = LinearLayoutManager(activity)
                binding.skillView.adapter = skillsListAdapter
                skillsListAdapter.setUserSkills(it.skills.map { s -> s.toString() })
                binding.skillView.isNestedScrollingEnabled = false
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Unexpected error", Toast.LENGTH_LONG).show()
        }
        updateAllSkills()

        binding.buttonRate.setOnClickListener {
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
        FirebaseFirestore.getInstance().collection("skills").get().addOnSuccessListener {
            val map = mutableMapOf<String, SkillData>()

            it.forEach { d -> map[d.id] = d.toSkillData() }
            skillsListAdapter.setAllSkills(map)
        }
    }


}