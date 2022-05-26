package it.polito.timebanking.ui.offers

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentOfferDetailBinding
import it.polito.timebanking.model.chat.JobData
import it.polito.timebanking.model.profile.ProfileViewModel
import it.polito.timebanking.model.profile.ageFormatter
import it.polito.timebanking.model.profile.fullNameFormatter
import it.polito.timebanking.model.timeslot.*

class OfferDetailFragment : Fragment() {
    private val vmTimeslot by viewModels<TimeslotViewModel>()
    private var _binding: FragmentOfferDetailBinding? = null
    private var favList = mutableListOf<String>()
    private val binding get() = _binding!!
    private var fav = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idTimeslot = requireArguments().getString("id_timeslot")!!
        val otherUserID = requireArguments().getString("id_user")!!
        val userID = FirebaseAuth.getInstance().currentUser!!.uid

        if (otherUserID == userID)
            Toast.makeText(context, "Your own offer!", Toast.LENGTH_SHORT).show()
        FirebaseFirestore.getInstance().collection("users")
            .document(otherUserID).get().addOnSuccessListener { user ->
                binding.UserFullName.text = fullNameFormatter(user.get("fullName").toString())
                binding.UserAge.text = ageFormatter(user.get("age").toString())
                binding.UserDescription.text = descriptionFormatter(user.get("description").toString())

                vmTimeslot.get(idTimeslot).observe(viewLifecycleOwner) {
                    binding.Title.text = titleFormatter(it.title)
                    binding.Description.text = descriptionFormatter(it.description)
                    binding.Date.text = dateFormatter(it.date)
                    binding.Duration.text = durationMinuteFormatter(resources, it.duration)
                    binding.Location.text = locationFormatter(it.location)
                }
            }
        FirebaseFirestore.getInstance().collection("users")
            .document(userID).get().addOnSuccessListener { d ->
                val myList = d.get("favorites") as MutableList<*>
                favList = myList.map { it.toString() }.toMutableList()
                if (favList.contains(idTimeslot)) {
                    fav = 1
                    requireActivity().invalidateOptionsMenu()
                }
                if (userID == otherUserID) {
                    fav = 2
                    requireActivity().invalidateOptionsMenu()
                }
            }

        binding.chatStartButton.isVisible = userID != otherUserID

        binding.chatStartButton.setOnClickListener {
            FirebaseFirestore.getInstance().collection("jobs")
                .whereEqualTo("timeslotID", idTimeslot).whereArrayContains("users", userID).get()
                .addOnSuccessListener { ext ->
                    if (ext.isEmpty) {
                        val jobData = JobData(
                            idTimeslot,
                            emptyList<String>(),
                            0,
                            otherUserID,
                            userID,
                            listOf(
                                otherUserID,
                                userID
                            ),
                            JobStatus.INIT,
                            "",
                            ""
                        )
                        FirebaseFirestore.getInstance().collection("jobs").add(jobData)
                            .addOnSuccessListener { int ->
                                findNavController().navigate(
                                    R.id.offer_to_job,
                                    bundleOf(
                                        "otherUserName" to binding.UserFullName.text,
                                        "jobID" to int.id
                                    )
                                )
                            }
                    } else {
                        val chat = ext.first()
                        findNavController().navigate(
                            R.id.offer_to_job,
                            bundleOf(
                                "otherUserName" to binding.UserFullName.text,
                                "jobID" to chat.id
                            )
                        )
                    }
                }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        when (fav) {
            1 -> {
                menu.findItem(R.id.action_fav_on).isVisible = true
                menu.findItem(R.id.action_fav_off).isVisible = false
            }
            0 -> {
                menu.findItem(R.id.action_fav_on).isVisible = false
                menu.findItem(R.id.action_fav_off).isVisible = true
            }
            else -> {
                menu.findItem(R.id.action_fav_on).isVisible = false
                menu.findItem(R.id.action_fav_off).isVisible = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_fav_off -> {
                fav = 1
                requireActivity().invalidateOptionsMenu()
                favList.add(requireArguments().getString("id_timeslot").toString())
                updateUserData(favList)
                Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
            R.id.action_fav_on -> {
                fav = 0
                requireActivity().invalidateOptionsMenu()
                favList.remove(requireArguments().getString("id_timeslot").toString())
                updateUserData(favList)
                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUserData(newFavList: List<String>) {
        val vm by viewModels<ProfileViewModel>()
        vm.get(FirebaseAuth.getInstance().currentUser!!.uid).observe(viewLifecycleOwner) {
            it.favorites = newFavList

            vm.update(
                FirebaseAuth.getInstance().currentUser!!.uid,
                it.fullName,
                it.nickName,
                it.age,
                it.email,
                it.location,
                it.description,
                newFavList
            )
        }
    }
}