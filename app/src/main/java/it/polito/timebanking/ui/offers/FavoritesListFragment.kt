package it.polito.timebanking.ui.offers

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentOffersBinding
import it.polito.timebanking.model.timeslot.toTimeslotData

class FavoritesListFragment : Fragment() {

    private var _binding: FragmentOffersBinding? = null
    private val offersListAdapter = OffersListAdapter("Fav")
    private val binding get() = _binding!!
    private val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOffersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var favorites: MutableList<*>
        binding.timeslotRecycler.layoutManager = LinearLayoutManager(activity)
        binding.timeslotRecycler.adapter = offersListAdapter
        binding.buttonAdd.isVisible = false

        FirebaseFirestore.getInstance().collection("users").document(firebaseUser).addSnapshotListener { t, _ ->
                if (t != null) {
                    favorites = t.get("favorites") as MutableList<*>
                    favorites.forEach { f ->
                        FirebaseFirestore.getInstance().collection("timeslots").document(f.toString()).get().addOnSuccessListener { t ->
                                offersListAdapter.addTimeslots(Pair(t.id, t.toTimeslotData()))
                            }
                    }
                    binding.nothingToShow.text = String.format(resources.getString(R.string.no_favorites))
                    binding.nothingToShow.isVisible = favorites.size == 0
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