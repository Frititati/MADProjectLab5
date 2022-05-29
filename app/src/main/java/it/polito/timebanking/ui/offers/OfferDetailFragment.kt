package it.polito.timebanking.ui.offers

import android.os.Bundle
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
import it.polito.timebanking.model.profile.ageFormatter
import it.polito.timebanking.model.profile.fullNameFormatter
import it.polito.timebanking.model.timeslot.*
import it.polito.timebanking.ui.messages.JobStatus
import java.text.DecimalFormat

class OfferDetailFragment : Fragment() {
    private val vmTimeslot by viewModels<TimeslotViewModel>()
    private var _binding: FragmentOfferDetailBinding? = null
    private val binding get() = _binding!!
    private val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idTimeslot = requireArguments().getString("id_timeslot")!!
        val otherUserID = requireArguments().getString("id_user")!!

        if (otherUserID == firebaseUserID) Toast.makeText(context, "Your own offer!", Toast.LENGTH_SHORT).show()
        FirebaseFirestore.getInstance().collection("users").document(otherUserID).get().addOnSuccessListener { user ->
            binding.UserFullName.text = fullNameFormatter(user.get("fullName").toString())
            binding.UserAge.text = ageFormatter(user.get("age").toString())
            binding.UserDescription.text = descriptionFormatter(user.get("description").toString())
            val score = user.getLong("score") ?: 0
            val jobsRated = user.getLong("jobsRated") ?: 0
            if (jobsRated != 0L) {
                val f = DecimalFormat("#.0")
                binding.userRating.text = f.format(((score / jobsRated) / 10.0)).toString()
            }
            vmTimeslot.get(idTimeslot).observe(viewLifecycleOwner) {
                binding.Title.text = titleFormatter(it.title)
                binding.Description.text = descriptionFormatter(it.description)
                binding.Date.text = dateFormatter(it.date)
                binding.Duration.text = durationMinuteFormatter(it.duration)
                binding.Location.text = locationFormatter(it.location)
            }
        }

        binding.chatStartButton.isVisible = firebaseUserID != otherUserID

        binding.chatStartButton.setOnClickListener {
            var jobExists = false
            FirebaseFirestore.getInstance().collection("jobs").whereEqualTo("timeslotID", idTimeslot).whereArrayContains("users", firebaseUserID).get().addOnSuccessListener { ext ->
                ext.forEach {
                    if (it.getString("jobStatus") != JobStatus.COMPLETED.toString()) {
                        jobExists = true
                    }
                }
                if (!jobExists) {
                    val jobData = JobData(idTimeslot, emptyList<String>(), System.currentTimeMillis(), otherUserID, firebaseUserID, listOf(otherUserID, firebaseUserID), JobStatus.INIT, "", "")
                    FirebaseFirestore.getInstance().collection("jobs").add(jobData).addOnSuccessListener {
                        findNavController().navigate(R.id.offer_to_job, bundleOf("otherUserName" to binding.UserFullName.text, "jobID" to it.id))
                    }
                } else {
                    val chat = ext.first()
                    findNavController().navigate(R.id.offer_to_job, bundleOf("otherUserName" to binding.UserFullName.text, "jobID" to chat.id))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}