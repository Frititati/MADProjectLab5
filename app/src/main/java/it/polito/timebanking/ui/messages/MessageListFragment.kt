package it.polito.timebanking.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentMessagesBinding
import it.polito.timebanking.model.chat.JobData
import it.polito.timebanking.model.chat.JobViewModel
import it.polito.timebanking.model.chat.MessageViewModel
import it.polito.timebanking.model.profile.ProfileData
import it.polito.timebanking.model.profile.ProfileViewModel
import it.polito.timebanking.model.profile.toUserProfileData
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.TimeslotViewModel
import it.polito.timebanking.model.timeslot.toTimeslotData

class MessageListFragment : Fragment() {
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val vmMessages by viewModels<MessageViewModel>()
    private val vmJob by viewModels<JobViewModel>()
    private val vmTimeslot by viewModels<TimeslotViewModel>()
    private val vmProfile by viewModels<ProfileViewModel>()
    private val messageListAdapter = MessageListAdapter()
    private lateinit var jobID: String
    private lateinit var drawerListener: NavBarUpdater
    private lateinit var job: JobData
    private lateinit var timeslot: TimeslotData
    private lateinit var userConsumer: ProfileData
    var userIsProducer = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        jobID = requireArguments().getString("jobID", "").toString()
        drawerListener = context as NavBarUpdater
        drawerListener.setTitleWithSkill("Chat with " + requireArguments().getString("otherUserName"))
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )

        val userID = FirebaseAuth.getInstance().currentUser!!.uid

        vmJob.get(jobID).observe(viewLifecycleOwner) { jobIt ->
            if (jobIt != null) {
                job = jobIt
                userIsProducer = (userID == job.userProducerID)
                Log.d("test", "user is producer? $userIsProducer")

                vmTimeslot.get(job.timeslotID).observe(viewLifecycleOwner) { timeslotIt ->
                    if (timeslotIt != null) {
                        timeslot = timeslotIt

                        vmProfile.get(job.userConsumerID).observe(viewLifecycleOwner) { profileIt ->
                            if (profileIt != null) {
                                userConsumer = profileIt

                                updateButtons()
                            }
                        }
                    }
                }
            }
        }

        binding.messageListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.messageListRecycler.adapter = messageListAdapter

        vmMessages.getMessages(jobID).observe(viewLifecycleOwner) {
            binding.messageListRecycler.scrollToPosition(messageListAdapter.itemCount)
            messageListAdapter.setMessages(it.sortedBy { a -> a.sentAt }.toMutableList())
        }

        binding.buttonSend.setOnClickListener {
            vmMessages.addMessage(
                userID,
                jobID,
                binding.writeMessage.text.toString(),
                System.currentTimeMillis(),
                false
            )
            binding.writeMessage.setText("")
        }

        binding.buttonOffer.setOnClickListener {
            findNavController().navigate(
                R.id.job_to_offer, bundleOf(
                    "id_timeslot" to job.timeslotID, "id_user" to job.userProducerID
                )
            )
        }

        binding.buttonRequest.setOnClickListener {
            FirebaseFirestore.getInstance().collection("jobs").document(jobID)
                .update("jobStatus", "REQUESTED")
                .addOnSuccessListener {
                    binding.buttonRequest.visibility = View.GONE
                }
            vmMessages.addMessage(
                userID,
                jobID,
                "Request for Job was Sent",
                System.currentTimeMillis(),
                true
            )
        }

        binding.buttonAccept.setOnClickListener {
            if (userConsumer.time >= timeslot.duration) {
                // userconsumer does not have enough time
                userConsumer.time = userConsumer.time - timeslot.duration
                FirebaseFirestore.getInstance().collection("users")
                    .document(job.userConsumerID)
                    .set(userConsumer).addOnSuccessListener {

                        timeslot.booked = true
                        FirebaseFirestore.getInstance().collection("timeslots")
                            .document(job.timeslotID)
                            .set(timeslot).addOnSuccessListener {

                                FirebaseFirestore.getInstance().collection("jobs")
                                    .document(jobID)
                                    .update("jobStatus", "ACCEPTED").addOnSuccessListener {
                                        vmMessages.addMessage(
                                            userID,
                                            jobID,
                                            "Job Request was Accepted",
                                            System.currentTimeMillis(),
                                            true
                                        )
                                    }
                            }
                    }
            } else {
                Log.d("test", "there is a problem with the time and durations2")
                // TODO Error message
            }

            // TODO error handling
        }

        binding.buttonReject.setOnClickListener {
            FirebaseFirestore.getInstance().collection("jobs").document(jobID)
                .update("jobStatus", "REJECTED")
                .addOnSuccessListener {
                    vmMessages.addMessage(
                        userID,
                        jobID,
                        "Job Request was Rejected",
                        System.currentTimeMillis(),
                        true
                    )
                }
        }

        binding.buttonJobStart.setOnClickListener {
            FirebaseFirestore.getInstance().collection("jobs").document(jobID)
                .update("jobStatus", "STARTED")
                .addOnSuccessListener {
                    vmMessages.addMessage(
                        userID,
                        jobID,
                        "Job has Started",
                        System.currentTimeMillis(),
                        true
                    )
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateButtons() {
        when (job.jobStatus) {
            "INIT" -> {
                if (userIsProducer) {
                    binding.buttonRequest.visibility = View.GONE
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                    binding.buttonJobEnd.visibility = View.GONE
                    binding.buttonJobStart.visibility = View.GONE
                    binding.buttonRate.visibility = View.GONE
                } else {
                    // TODO calculate if user has money
                    binding.buttonRequest.visibility = View.VISIBLE
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                    binding.buttonJobEnd.visibility = View.GONE
                    binding.buttonJobStart.visibility = View.GONE
                    binding.buttonRate.visibility = View.GONE
                }
            }
            "REQUESTED" -> {
                if (userIsProducer) {
                    binding.buttonRequest.visibility = View.GONE
                    binding.buttonAccept.visibility = View.VISIBLE
                    binding.buttonReject.visibility = View.VISIBLE
                    binding.buttonJobEnd.visibility = View.GONE
                    binding.buttonJobStart.visibility = View.GONE
                    binding.buttonRate.visibility = View.GONE
                } else {
                    binding.buttonRequest.visibility = View.GONE
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                    binding.buttonJobEnd.visibility = View.GONE
                    binding.buttonJobStart.visibility = View.GONE
                    binding.buttonRate.visibility = View.GONE
                }
            }
            "ACCEPTED" -> {
                if (userIsProducer) {
                    binding.buttonRequest.visibility = View.GONE
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                    binding.buttonJobEnd.visibility = View.GONE
                    binding.buttonJobStart.visibility = View.GONE
                    binding.buttonRate.visibility = View.GONE
                } else {
                    binding.buttonRequest.visibility = View.GONE
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                    binding.buttonJobEnd.visibility = View.GONE
                    binding.buttonJobStart.visibility = View.VISIBLE
                    binding.buttonRate.visibility = View.GONE
                }
            }
            "STARTED" -> {
                if (userIsProducer) {
                    binding.buttonRequest.visibility = View.GONE
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                    binding.buttonJobEnd.visibility = View.VISIBLE
                    binding.buttonJobStart.visibility = View.GONE
                    binding.buttonRate.visibility = View.GONE
                } else {
                    binding.buttonRequest.visibility = View.GONE
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                    binding.buttonJobEnd.visibility = View.VISIBLE
                    binding.buttonJobStart.visibility = View.GONE
                    binding.buttonRate.visibility = View.GONE
                }
            }
            "FINISHED" -> {
                if (userIsProducer) {
                    binding.buttonRequest.visibility = View.GONE
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                    binding.buttonJobEnd.visibility = View.GONE
                    binding.buttonJobStart.visibility = View.GONE
                    binding.buttonRate.visibility = View.VISIBLE
                } else {
                    binding.buttonRequest.visibility = View.GONE
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                    binding.buttonJobEnd.visibility = View.GONE
                    binding.buttonJobStart.visibility = View.GONE
                    binding.buttonRate.visibility = View.VISIBLE
                }
            }
        }
    }
}