package it.polito.timebanking.ui.all_timeslot

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.databinding.FragmentAdvertisementBinding
import it.polito.timebanking.model.timeslot.toTimeslotData

class FavoritesListFragment : Fragment() {

    private var _binding: FragmentAdvertisementBinding? = null
    private val advertisementListAdapter = AdvertisementListAdapter("Fav")
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvertisementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var favorites: MutableList<String>
        binding.timeslotRecycler.layoutManager = LinearLayoutManager(activity)
        binding.timeslotRecycler.adapter = advertisementListAdapter
        binding.buttonAdd.isVisible = false
        advertisementListAdapter.clear()

        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
                favorites = it.get("favorites") as MutableList<String>
                favorites.forEach { f ->
                    FirebaseFirestore.getInstance().collection("timeslots").document(f).get()
                        .addOnSuccessListener { t ->
                            advertisementListAdapter.addTimeslots(Pair(t.id, t.toTimeslotData()))
                        }
                }
                binding.nothingToShow.isVisible = favorites.size == 0
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