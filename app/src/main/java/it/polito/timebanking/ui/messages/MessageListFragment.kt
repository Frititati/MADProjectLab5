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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentMessagesBinding
import it.polito.timebanking.model.rating.RateData
import it.polito.timebanking.model.chat.JobData
import it.polito.timebanking.model.chat.JobViewModel
import it.polito.timebanking.model.chat.MessageViewModel
import it.polito.timebanking.model.profile.ProfileData
import it.polito.timebanking.model.profile.ProfileViewModel
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.TimeslotViewModel
import it.polito.timebanking.model.transaction.TransactionData

class MessageListFragment : Fragment() {
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val vmMessages by viewModels<MessageViewModel>()
    private val vmJob by viewModels<JobViewModel>()
    private val vmTimeslot by viewModels<TimeslotViewModel>()
    private val vmProfile by viewModels<ProfileViewModel>()
    private val messageListAdapter = MessageListAdapter()
    private val firebaseUserID = FirebaseAuth.getInstance().uid!!
    private var userIsProducer = false
    private lateinit var jobID: String
    private lateinit var listenerNavBar: NavBarUpdater
    private lateinit var job: JobData
    private lateinit var timeslot: TimeslotData
    private lateinit var userConsumer: ProfileData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        jobID = requireArguments().getString("jobID", "").toString()
        listenerNavBar = context as NavBarUpdater
        listenerNavBar.setNavBarTitle("Chat with " + requireArguments().getString("otherUserName"))
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        vmJob.get(jobID).observe(viewLifecycleOwner) { jobIt ->
            if (jobIt != null) {
                job = jobIt
                userIsProducer = (firebaseUserID == job.userProducerID)

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
            val message = binding.writeMessage.text.trim().toString()
            if (message.isNotEmpty()) {
                vmMessages.addMessage(firebaseUserID, jobID, message, System.currentTimeMillis(), false)
                binding.writeMessage.text.clear()
            }
            else Toast.makeText(context, "Cannot send empty message", Toast.LENGTH_SHORT).show()
            FirebaseFirestore.getInstance().collection("jobs").document(jobID).update("lastUpdate", System.currentTimeMillis())
            if(userIsProducer)
                FirebaseFirestore.getInstance().collection("jobs").document(jobID).update("seenByConsumer",false)
            else
                FirebaseFirestore.getInstance().collection("jobs").document(jobID).update("seenByProducer",false)

        }

        binding.buttonOffer.setOnClickListener {
            findNavController().navigate(R.id.job_to_offer, bundleOf("id_timeslot" to job.timeslotID, "id_user" to job.userProducerID))
        }

        binding.buttonRequest.setOnClickListener {
            FirebaseFirestore.getInstance().collection("timeslots").document(job.timeslotID).get().addOnSuccessListener { ts ->
                if (ts.getBoolean("available") == true) {
                    FirebaseFirestore.getInstance().collection("users").document(job.userConsumerID).get().addOnSuccessListener {
                        if (enoughTime(it.getLong("time") ?: 0)) updateJobStatus(JobStatus.REQUESTED, "Job was REQUESTED")
                        else Toast.makeText(context, "Sorry, you don't have enough time", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e -> Log.w("warn", "Error with users $e") }
                }
                else {
                    Snackbar.make(binding.root, "The timeslot is temporarily unavailable. Please try again later", Snackbar.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e -> Log.w("warn", "Error with timeslots $e") }
        }

        binding.buttonAccept.setOnClickListener {
            FirebaseFirestore.getInstance().collection("users").document(job.userConsumerID).get().addOnSuccessListener {
                if (enoughTime(it.getLong("time") ?: 0L)) {
                    FirebaseFirestore.getInstance().collection("users").document(firebaseUserID).update("time", FieldValue.increment(timeslot.duration)).addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("users").document(job.userConsumerID).update("time", FieldValue.increment(-timeslot.duration)).addOnSuccessListener {
                            timeslot.available = false
                            FirebaseFirestore.getInstance().collection("timeslots").document(job.timeslotID).update("available", false).addOnSuccessListener {
                                updateJobStatus(JobStatus.ACCEPTED, "Job was ACCEPTED")
                                addTransaction(timeslot.title, job.userProducerID, timeslot.duration)
                                addTransaction(timeslot.title, job.userConsumerID, -timeslot.duration)
                                if (userIsProducer)
                                    Snackbar.make(binding.root, "You received: ${timeFormatter(timeslot.duration)}", Snackbar.LENGTH_SHORT).show()
                            }.addOnFailureListener { e -> Log.w("warn", "Error with users $e") }
                        }.addOnFailureListener { e -> Log.w("warn", "Error with users $e") }
                    }.addOnFailureListener { e -> Log.w("warn", "Error with users $e") }
                }
                else {
                    Toast.makeText(context, "Couldn't accept. Consumer is out of time", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e -> Log.w("warn", "Error with users $e") }
        }

        binding.buttonReject.setOnClickListener {
            FirebaseFirestore.getInstance().collection("users").document(job.userProducerID).update("activeProducingJobs",FieldValue.increment(-1))
            FirebaseFirestore.getInstance().collection("users").document(job.userConsumerID).update("activeConsumingJobs",FieldValue.increment(-1))
            updateJobStatus(JobStatus.REJECTED, "Job was REJECTED")
        }

        binding.buttonJobStart.setOnClickListener {
            Snackbar.make(binding.root, "You granted: ${timeFormatter(timeslot.duration)}", Snackbar.LENGTH_SHORT).show()
            updateJobStatus(JobStatus.STARTED, "Job was STARTED")
        }
        binding.buttonJobEnd.setOnClickListener {
            FirebaseFirestore.getInstance().collection("timeslots").document(job.timeslotID).update("available", true).addOnSuccessListener {
                updateJobStatus(JobStatus.DONE, "Job was DONE")
            }.addOnFailureListener { e -> Log.w("warn", "Error with timeslots $e") }
        }

        binding.buttonRate.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dialog_rate_user, null)
            dialog.setTitle("Rating")
            dialog.setView(dialogView)

            dialog.setPositiveButton("Confirm") { _, _ ->
                val rating = dialogView.findViewById<RatingBar>(R.id.ratingBar).rating.toDouble()
                val comment = dialogView.findViewById<EditText>(R.id.comment).text.toString()
                if (userIsProducer) {
                    val rate = RateData(rating, comment, timeslot.title, job.userProducerID, job.userConsumerID)
                    FirebaseFirestore.getInstance().collection("ratings").add(rate).addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("users").document(job.userConsumerID).update("jobsRatedAsConsumer", FieldValue.increment(1), "scoreAsConsumer", FieldValue.increment(rating))
                        Snackbar.make(binding.root, "Rated successfully", Snackbar.LENGTH_SHORT).show()
                    }.addOnFailureListener { e -> Log.w("warn", "Error with jobs $e") }
                }
                else {
                    val rate = RateData(rating, comment, timeslot.title, job.userConsumerID, job.userProducerID)
                    FirebaseFirestore.getInstance().collection("ratings").add(rate).addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("users").document(job.userProducerID).update("jobsRatedAsProducer", FieldValue.increment(1), "scoreAsProducer", FieldValue.increment(rating))
                        Snackbar.make(binding.root, "Rated successfully", Snackbar.LENGTH_SHORT).show()
                    }.addOnFailureListener { e -> Log.w("warn", "Error with ratings $e") }
                }

                if (userIsProducer) {
                    FirebaseFirestore.getInstance().collection("users").document(job.userProducerID).update("activeProducingJobs",FieldValue.increment(-1))
                    if (job.jobStatus == JobStatus.DONE)
                        updateJobStatus(JobStatus.RATED_BY_PRODUCER, "Job was RATED (by producer)")
                    else {
                        vmMessages.addMessage(firebaseUserID, jobID, "Job was RATED (by producer)", System.currentTimeMillis(), true)
                        updateJobStatus(JobStatus.COMPLETED, "Job was CONCLUDED")
                    }
                }
                else {
                    FirebaseFirestore.getInstance().collection("users").document(job.userConsumerID).update("activeConsumingJobs",FieldValue.increment(-1))
                    if (job.jobStatus == JobStatus.DONE)
                        updateJobStatus(JobStatus.RATED_BY_CONSUMER, "Job was RATED (by consumer)")
                    else {
                        vmMessages.addMessage(firebaseUserID, jobID, "Job was RATED (by consumer)", System.currentTimeMillis(), true)
                        updateJobStatus(JobStatus.COMPLETED, "Job was CONCLUDED")
                    }
                }
            }
            dialog.setNegativeButton("Cancel") { _, _ -> }
            dialog.create().show()
        }
    }

    private fun enoughTime(userTime: Long): Boolean {
        return timeslot.duration <= userTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateJobStatus(status: JobStatus, message: String) {
        FirebaseFirestore.getInstance().collection("jobs").document(jobID).update("jobStatus", status, "lastUpdate", System.currentTimeMillis()).addOnSuccessListener {
            vmMessages.addMessage(firebaseUserID, jobID, message, System.currentTimeMillis(), true)
        }.addOnFailureListener { e -> Log.w("warn", "Error with ratings $e") }
    }

    private fun updateButtons() {
        when (job.jobStatus) {
            JobStatus.INIT -> {
                if (userIsProducer) {
                    updateButtonStatus(requested = false, accept = false, reject = false, start = false, end = false, rate = false)
                }
                else {
                    updateButtonStatus(requested = true, accept = false, reject = false, start = false, end = false, rate = false)
                }
            }
            JobStatus.REQUESTED -> {
                if (userIsProducer) {
                    updateButtonStatus(requested = false, accept = true, reject = true, start = false, end = false, rate = false)
                }
                else {
                    updateButtonStatus(requested = false, accept = false, reject = false, start = false, end = false, rate = false)
                }
            }
            JobStatus.ACCEPTED -> {
                updateButtonStatus(requested = false, accept = false, reject = false, start = !userIsProducer, end = false, rate = false)
            }
            JobStatus.STARTED -> {
                updateButtonStatus(requested = false, accept = false, reject = false, start = false, end = true, rate = false)
            }
            JobStatus.DONE -> {
                updateButtonStatus(requested = false, accept = false, reject = false, start = false, end = false, rate = true)
            }
            JobStatus.RATED_BY_CONSUMER -> {
                updateButtonStatus(requested = false, accept = false, reject = false, start = false, end = false, rate = userIsProducer)
            }
            JobStatus.RATED_BY_PRODUCER -> {
                updateButtonStatus(requested = false, accept = false, reject = false, start = false, end = false, rate = !userIsProducer)
            }
            else -> {
                updateButtonStatus(requested = false, accept = false, reject = false, start = false, end = false, rate = false)
            }
        }
    }

    private fun updateButtonStatus(requested: Boolean, accept: Boolean, reject: Boolean, start: Boolean, end: Boolean, rate: Boolean) {
        binding.buttonRequest.visibility = if (requested) View.VISIBLE else View.GONE
        binding.buttonAccept.visibility = if (accept) View.VISIBLE else View.GONE
        binding.buttonReject.visibility = if (reject) View.VISIBLE else View.GONE
        binding.buttonJobStart.visibility = if (start) View.VISIBLE else View.GONE
        binding.buttonJobEnd.visibility = if (end) View.VISIBLE else View.GONE
        binding.buttonRate.visibility = if (rate) View.VISIBLE else View.GONE
    }

    private fun timeFormatter(time: Long): String {
        val h = if (time / 60L == 1L) "1 hour"
        else "${time / 60L} hours"
        val m = if (time % 60L == 1L) "1 minute"
        else "${time % 60L} minutes"
        return if (h == "0 hours") m
        else "$h, $m"
    }

    private fun addTransaction(jobTitle: String, userID: String, time: Long) {
        val transaction = TransactionData(
            jobTitle,
            userID,
            time,
            false,
            System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("transactions").add(transaction).addOnSuccessListener {
        }.addOnFailureListener { e -> Log.w("warn", "Error with transactions $e") }
    }

    override fun onPause() {
        super.onPause()
        if(userIsProducer)
            FirebaseFirestore.getInstance().collection("jobs").document(jobID).update("seenByProducer",true)
        else
            FirebaseFirestore.getInstance().collection("jobs").document(jobID).update("seenByConsumer",true)


    }
}