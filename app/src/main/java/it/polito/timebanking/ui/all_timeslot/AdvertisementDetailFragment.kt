package it.polito.timebanking.ui.all_timeslot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentAdvertisementDetailBinding
import it.polito.timebanking.model.profile.ageFormatter
import it.polito.timebanking.model.profile.fullNameFormatter
import it.polito.timebanking.model.timeslot.*

class AdvertisementDetailFragment : Fragment() {
    private val timeslotVM by viewModels<TimeslotViewModel>()
    private var _binding: FragmentAdvertisementDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvertisementDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idTimeslot = arguments?.getString("id_timeslot")
        val idUser = arguments?.getString("id_user")
        FirebaseFirestore.getInstance().collection("users")
            .document(idUser!!).get().addOnSuccessListener { user ->
                binding.UserNickname.text = fullNameFormatter(user.get("fullName").toString())
                binding.UserAge.text = ageFormatter(user.get("age").toString())
                binding.UserDescription.text = descriptionFormatter(user.get("description").toString())

                timeslotVM.get(idTimeslot!!).observe(viewLifecycleOwner) {
                    binding.Title.text = titleFormatter(it.title)
                    binding.Description.text = descriptionFormatter(it.description)
                    binding.Date.text = dateFormatter(it.date)
                    binding.Duration.text = durationMinuteFormatter(resources, it.duration)
                }
            }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}