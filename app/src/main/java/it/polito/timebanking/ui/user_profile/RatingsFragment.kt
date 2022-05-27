package it.polito.timebanking.ui.user_profile

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentRatingsBinding
import it.polito.timebanking.model.rating.RateData
import it.polito.timebanking.model.rating.RateViewModel

class RatingsFragment : Fragment() {

    private var _binding: FragmentRatingsBinding? = null
    private val binding get() = _binding!!
    private var ratingsListAdapter = RatingsAdapter()
    private var showReceivedOption = false
    private var ratingList = emptyList<Pair<String, RateData>>()
    private val rateVM by viewModels<RateViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        binding.rateListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.rateListRecycler.adapter = ratingsListAdapter

        rateVM.getRatings(FirebaseAuth.getInstance().currentUser!!.uid)
            .observe(viewLifecycleOwner) {
                ratingList = it
                ratingsListAdapter.setRatings(
                    it as MutableList<Pair<String, RateData>>,
                    !showReceivedOption
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_given_ratings).isVisible = showReceivedOption
        menu.findItem(R.id.action_received_ratings).isVisible = !showReceivedOption
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_received_ratings -> {
                ratingsListAdapter.setRatings(
                    ratingList as MutableList<Pair<String, RateData>>,
                    showReceivedOption
                )
                showReceivedOption = true
                requireActivity().invalidateOptionsMenu()
                true
            }
            R.id.action_given_ratings -> {
                ratingsListAdapter.setRatings(
                    ratingList as MutableList<Pair<String, RateData>>,
                    showReceivedOption
                )
                showReceivedOption = false
                requireActivity().invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}