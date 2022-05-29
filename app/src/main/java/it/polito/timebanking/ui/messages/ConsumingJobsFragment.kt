package it.polito.timebanking.ui.messages

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentConsumingJobsBinding
import it.polito.timebanking.model.chat.JobData
import it.polito.timebanking.model.chat.JobViewModel

class ConsumingJobsFragment : Fragment() {
    private var _binding: FragmentConsumingJobsBinding? = null
    private val binding get() = _binding!!
    private var jobsListAdapter = ConsumingJobsAdapter()
    private var allJobs = mutableListOf<Pair<String, JobData>>()
    private val jobVM by viewModels<JobViewModel>()
    private val firebaseUserID = FirebaseAuth.getInstance().uid!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsumingJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        binding.chatListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.chatListRecycler.adapter = jobsListAdapter
        binding.nothingToShow.text = resources.getString(R.string.no_consuming_jobs)

        jobVM.getConsumingJobs(firebaseUserID).observe(viewLifecycleOwner) {
            jobsListAdapter.setChats(it as MutableList<Pair<String, JobData>>)
            binding.nothingToShow.isVisible = it.isEmpty()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_filterJobs).subMenu.clear()
        menu.findItem(R.id.action_filterJobs).isVisible = true
        menu.findItem(R.id.action_filterJobs).subMenu.add(0, JobStatus.REQUESTED.ordinal, 0, "Requested")
        menu.findItem(R.id.action_filterJobs).subMenu.add(0, JobStatus.ACCEPTED.ordinal, 0, "Accepted")
        menu.findItem(R.id.action_filterJobs).subMenu.add(0, JobStatus.DONE.ordinal, 0, "To be rated")
        menu.findItem(R.id.action_filterJobs).subMenu.add(0, JobStatus.COMPLETED.ordinal, 0, "Completed")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            JobStatus.REQUESTED.ordinal -> {
                binding.nothingToShow.isVisible = false

                val n = jobsListAdapter.filterBy(allJobs, JobStatus.REQUESTED)
                if (n == 0) {
                    binding.nothingToShow.isVisible = true
                    binding.nothingToShow.text = String.format(resources.getString(R.string.no_jobs_with_status), JobStatus.REQUESTED)
                }
                true
            }
            JobStatus.ACCEPTED.ordinal -> {
                binding.nothingToShow.isVisible = false
                val n = jobsListAdapter.filterBy(allJobs, JobStatus.ACCEPTED)
                if (n == 0) {
                    binding.nothingToShow.isVisible = true
                    binding.nothingToShow.text = String.format(resources.getString(R.string.no_jobs_with_status), JobStatus.ACCEPTED)
                }
                true
            }
            JobStatus.DONE.ordinal -> {
                binding.nothingToShow.isVisible = false
                val n = jobsListAdapter.filterBy(allJobs, JobStatus.DONE)
                if (n == 0) {
                    binding.nothingToShow.isVisible = true
                    binding.nothingToShow.text = String.format("You don't have any job to rate")
                }
                true
            }
            JobStatus.COMPLETED.ordinal -> {
                binding.nothingToShow.isVisible = false
                val n = jobsListAdapter.filterBy(allJobs, JobStatus.COMPLETED)
                if (n == 0) {
                    binding.nothingToShow.isVisible = true
                    binding.nothingToShow.text = String.format(resources.getString(R.string.no_jobs_with_status), JobStatus.COMPLETED)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}