package it.polito.timebanking.ui.all_timeslot

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentAdvertisementDetailBinding
import it.polito.timebanking.model.profile.ProfileViewModel
import it.polito.timebanking.model.profile.ageFormatter
import it.polito.timebanking.model.profile.fullNameFormatter
import it.polito.timebanking.model.timeslot.*

class AdvertisementDetailFragment : Fragment() {
    private val timeslotVM by viewModels<TimeslotViewModel>()
    private var _binding: FragmentAdvertisementDetailBinding? = null
    private var favList = mutableListOf<String>()
    private val binding get() = _binding!!
    private var fav = false

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
        val idTimeslot = requireArguments().getString("id_timeslot")
        val idUser = requireArguments().getString("id_user")
        FirebaseFirestore.getInstance().collection("users")
            .document(idUser!!).get().addOnSuccessListener { user ->
                binding.UserFullName.text = fullNameFormatter(user.get("fullName").toString())
                binding.UserAge.text = ageFormatter(user.get("age").toString())
                binding.UserDescription.text =
                    descriptionFormatter(user.get("description").toString())

                timeslotVM.get(idTimeslot!!).observe(viewLifecycleOwner) {
                    binding.Title.text = titleFormatter(it.title)
                    binding.Description.text = descriptionFormatter(it.description)
                    binding.Date.text = dateFormatter(it.date)
                    binding.Duration.text = durationMinuteFormatter(resources, it.duration)
                }
            }
        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
            favList = it.get("favorites") as MutableList<String>
            if(favList.contains(idTimeslot)){
                fav = true
                requireActivity().invalidateOptionsMenu()
            }
        }

        binding.chatStartButton.setOnClickListener {
            findNavController().navigate(
                R.id.ad_to_chat,
                bundleOf("user" to "Chat with ${binding.UserFullName.text}")
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (fav) {
            menu.findItem(R.id.action_fav_on).isVisible = true
            menu.findItem(R.id.action_fav_off).isVisible = false
        } else {
            menu.findItem(R.id.action_fav_on).isVisible = false
            menu.findItem(R.id.action_fav_off).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_fav_off -> {
                fav = true
                requireActivity().invalidateOptionsMenu()
                favList.add(requireArguments().getString("id_timeslot").toString())
                updateUserData(favList)
                Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
            R.id.action_fav_on -> {
                fav = false
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

    private fun updateUserData(newFavList:List<String>){
        val vm by viewModels<ProfileViewModel>()
            vm.get(FirebaseAuth.getInstance().currentUser!!.uid).observe(viewLifecycleOwner){
                vm.update(
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    it.fullName.toString(),
                    it.nickName.toString(),
                    it.age.toString(),
                    it.email.toString(),
                    it.location.toString(),
                    it.description.toString(),
                    newFavList
                )
            }
    }
}