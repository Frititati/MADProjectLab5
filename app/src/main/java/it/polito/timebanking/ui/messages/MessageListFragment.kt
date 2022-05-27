package it.polito.timebanking.ui.messages

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentMessagesBinding
import it.polito.timebanking.model.chat.JobData
import it.polito.timebanking.model.chat.JobViewModel
import it.polito.timebanking.model.chat.MessageViewModel
import it.polito.timebanking.model.profile.ProfileData
import it.polito.timebanking.model.profile.ProfileViewModel
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.TimeslotViewModel

class MessageListFragment : Fragment() {
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val vmMessages by viewModels<MessageViewModel>()
    private val vmJob by viewModels<JobViewModel>()
    private val vmTimeslot by viewModels<TimeslotViewModel>()
    private val vmProfile by viewModels<ProfileViewModel>()
    private val messageListAdapter = MessageListAdapter()
    private val userID = FirebaseAuth.getInstance().currentUser!!.uid
    private var ratedByProducer = false
    private var ratedByConsumer = false
    private var userIsProducer = false
    private lateinit var jobID: String
    private lateinit var drawerListener: NavBarUpdater
    private lateinit var job: JobData
    private lateinit var timeslot: TimeslotData
    private lateinit var userConsumer: ProfileData

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

        vmJob.get(jobID).observe(viewLifecycleOwner) { jobIt ->
            if (jobIt != null) {
                job = jobIt
                userIsProducer = (userID == job.userProducerID)

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
            val message = binding.writeMessage.text.toString()
            if (message.isNotEmpty()) {
                vmMessages.addMessage(
                    userID,
                    jobID,
                    message,
                    System.currentTimeMillis(),
                    false
                )
                binding.writeMessage.setText("")
            } else
                Toast.makeText(context, "Cannot send empty message", Toast.LENGTH_SHORT).show()

        }

        binding.buttonOffer.setOnClickListener {
            findNavController().navigate(
                R.id.job_to_offer, bundleOf(
                    "id_timeslot" to job.timeslotID, "id_user" to job.userProducerID
                )
            )
        }

        binding.buttonRequest.setOnClickListener {
            updateJobStatus(JobStatus.REQUESTED, "Job was REQUESTED")
        }

        binding.buttonAccept.setOnClickListener {
            if (userConsumer.time >= timeslot.duration) {
                FirebaseFirestore.getInstance().collection("users").document(userID)
                    .update("time", FieldValue.increment(timeslot.duration))
                    .addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(job.userConsumerID)
                            .update("time", FieldValue.increment(-timeslot.duration))
                            .addOnSuccessListener {
                                timeslot.available = false
                                FirebaseFirestore.getInstance().collection("timeslots")
                                    .document(job.timeslotID)
                                    .set(timeslot).addOnSuccessListener {
                                        updateJobStatus(JobStatus.ACCEPTED, "Job was ACCEPTED")
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
            FirebaseFirestore.getInstance().collection("timeslots")
                .document(job.timeslotID)
                .set(timeslot).addOnSuccessListener {
                    updateJobStatus(JobStatus.REJECTED, "Job was REJECTED")
                }
        }

        binding.buttonJobStart.setOnClickListener {
            updateJobStatus(JobStatus.STARTED, "Job is STARTED")
        }
        binding.buttonJobEnd.setOnClickListener {
            timeslot.available = true
            FirebaseFirestore.getInstance().collection("timeslots")
            .document(job.timeslotID)
            .set(timeslot).addOnSuccessListener {
                updateJobStatus(JobStatus.FINISHED, "Job is FINISHED")
            }
        }

        binding.buttonRate.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dialog_rate_user, null)
            dialog.setTitle("Rating")
            dialog.setView(dialogView)

            dialog.setPositiveButton("Confirm") { _, _ ->
                val rating = dialogView.findViewById<RatingBar>(R.id.ratingBar).rating.toInt()
                val comment = dialogView.findViewById<EditText>(R.id.comment).text.toString()
                if (userIsProducer) {
                    ratedByProducer = true
                    updateJobStatus(JobStatus.RATED, "Job was RATED (by producer)")
                } else {
                    ratedByConsumer = true
                    updateJobStatus(JobStatus.RATED, "Job was RATED (by consumer)")
                }
            }
            dialog.setNegativeButton("Cancel") { _, _ -> }
            dialog.create().show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateJobStatus(status: JobStatus, message: String) {
        FirebaseFirestore.getInstance().collection("jobs").document(jobID)
            .update("jobStatus", status).addOnSuccessListener {
                vmMessages.addMessage(
                    userID,
                    jobID,
                    message,
                    System.currentTimeMillis(),
                    true
                )
            }
    }

    private fun updateButtons() {
        when (job.jobStatus) {
            JobStatus.INIT -> {
                if (userIsProducer) {
                    updateButtonStatus(
                        requested = false,
                        accept = false,
                        reject = false,
                        start = false,
                        end = false,
                        rate = false
                    )
                } else {
                    // TODO calculate if user has money
                    updateButtonStatus(
                        requested = true,
                        accept = false,
                        reject = false,
                        start = false,
                        end = false,
                        rate = false
                    )
                }
            }
            JobStatus.REQUESTED -> {
                if (userIsProducer) {
                    updateButtonStatus(
                        requested = false,
                        accept = true,
                        reject = true,
                        start = false,
                        end = false,
                        rate = false
                    )
                } else {
                    updateButtonStatus(
                        requested = false,
                        accept = false,
                        reject = false,
                        start = false,
                        end = false,
                        rate = false
                    )
                }
            }
            JobStatus.ACCEPTED -> {
                updateButtonStatus(
                    requested = false,
                    accept = false,
                    reject = false,
                    start = !userIsProducer,
                    end = false,
                    rate = false
                )
            }
            JobStatus.STARTED -> {
                updateButtonStatus(
                    requested = false,
                    accept = false,
                    reject = false,
                    start = false,
                    end = true,
                    rate = false
                )
            }
            JobStatus.FINISHED -> {
                updateButtonStatus(
                    requested = false,
                    accept = false,
                    reject = false,
                    start = false,
                    end = false,
                    rate = true
                )
            }
            JobStatus.RATED -> {
                updateButtonStatus(
                    requested = false,
                    accept = false,
                    reject = false,
                    start = false,
                    end = false,
                    rate = showRateButton()
                )
            }
            JobStatus.REJECTED -> {
                updateButtonStatus(
                    requested = false,
                    accept = false,
                    reject = false,
                    start = false,
                    end = false,
                    rate = false
                )
            }
        }
    }

    private fun updateButtonStatus(
        requested: Boolean,
        accept: Boolean,
        reject: Boolean,
        start: Boolean,
        end: Boolean,
        rate: Boolean
    ) {

        binding.buttonRequest.visibility = if (requested) View.VISIBLE else View.GONE
        binding.buttonAccept.visibility = if (accept) View.VISIBLE else View.GONE
        binding.buttonReject.visibility = if (reject) View.VISIBLE else View.GONE
        binding.buttonJobStart.visibility = if (start) View.VISIBLE else View.GONE
        binding.buttonJobEnd.visibility = if (end) View.VISIBLE else View.GONE
        binding.buttonRate.visibility = if (rate) View.VISIBLE else View.GONE
    }

    private fun showRateButton(): Boolean {
        return (userIsProducer && !ratedByProducer) || (!userIsProducer && !ratedByConsumer)
    }

}