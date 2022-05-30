package it.polito.timebanking.ui.user_profile

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentRatingsBinding
import it.polito.timebanking.model.rating.RateData
import it.polito.timebanking.model.rating.RateViewModel

class RatingsFragment : Fragment() {

    private var _binding: FragmentRatingsBinding? = null
    private val binding get() = _binding!!
    private var ratingsListAdapter = RatingsAdapter()
    private var higher = 1
    private var lower = 2
    private var showingReceived = true
    private val firebaseUserID = FirebaseAuth.getInstance().uid!!
    private var ratingList = emptyList<Pair<String, RateData>>()
    private val rateVM by viewModels<RateViewModel>()
    private lateinit var listenerNavBar: NavBarUpdater

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        listenerNavBar = context as NavBarUpdater
        listenerNavBar.setNavBarTitle("Received Ratings")
        _binding = FragmentRatingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.rateListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.rateListRecycler.adapter = ratingsListAdapter

        rateVM.getRatings(firebaseUserID).observe(viewLifecycleOwner) {
            ratingList = it
            binding.nothingToShow.isVisible = it.isEmpty()
            binding.nothingToShow.text = if (showingReceived) String.format(resources.getString(R.string.no_ratings, "Received")) else String.format(resources.getString(R.string.no_ratings, "Given"))
            ratingsListAdapter.setRatings(it as MutableList<Pair<String, RateData>>, showingReceived)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_given_ratings).isVisible = showingReceived
        menu.findItem(R.id.action_received_ratings).isVisible = !showingReceived
        menu.findItem(R.id.action_sortRatings).subMenu.clear()
        menu.findItem(R.id.action_sortRatings).isVisible = true
        menu.findItem(R.id.action_sortRatings).subMenu.add(0, higher, 0, "Higher")
        menu.findItem(R.id.action_sortRatings).subMenu.add(0, lower, 0, "Lower")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_received_ratings -> {
                listenerNavBar.setNavBarTitle("Received Ratings")
                showingReceived = true
                val n = ratingsListAdapter.setRatings(ratingList as MutableList<Pair<String, RateData>>, showingReceived)
                if (n == 0) {
                    binding.nothingToShow.text = String.format(resources.getString(R.string.no_ratings, "Received"))
                    binding.nothingToShow.isVisible = true
                }
                requireActivity().invalidateOptionsMenu()
                true
            }
            R.id.action_given_ratings -> {
                listenerNavBar.setNavBarTitle("Given Ratings")
                showingReceived = false
                val n = ratingsListAdapter.setRatings(ratingList as MutableList<Pair<String, RateData>>, showingReceived)
                if (n == 0) {
                    binding.nothingToShow.text = String.format(resources.getString(R.string.no_ratings, "Given"))
                    binding.nothingToShow.isVisible = true
                }
                requireActivity().invalidateOptionsMenu()
                true
            }
            higher -> {
                ratingsListAdapter.sortByRate(true)
                true
            }
            lower -> {
                ratingsListAdapter.sortByRate(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}