package it.polito.timebanking.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import it.polito.timebanking.model.chat.MessageViewModel
import it.polito.timebanking.model.chat.toJobData
import it.polito.timebanking.model.profile.ProfileData
import it.polito.timebanking.model.profile.toUserProfileData
import it.polito.timebanking.model.timeslot.TimeslotData
import it.polito.timebanking.model.timeslot.toTimeslotData

class MessageListFragment : Fragment() {
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<MessageViewModel>()
    private val messageListAdapter = MessageListAdapter()
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

        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        var userIsProducer = false

        FirebaseFirestore.getInstance().collection("jobs")
            .document(jobID).get().addOnSuccessListener { jobIt ->
                job = jobIt.toJobData()

                FirebaseFirestore.getInstance().collection("timeslots")
                    .document(job.timeslotID!!).get().addOnSuccessListener { timeslotIt ->
                        timeslot = timeslotIt.toTimeslotData()

                        FirebaseFirestore.getInstance().collection("users")
                            .document(job.userConsumerID!!).get()
                            .addOnSuccessListener { userIt ->
                                userConsumer = userIt.toUserProfileData()

                                userIsProducer = (userID == job.userProducerID)
                                if (userIsProducer) {
                                    if (job.jobStatus == "REQUESTED") {
                                        binding.buttonAccept.isVisible = true
                                        binding.buttonReject.isVisible = true
                                    }
                                } else {
                                    if (job.jobStatus == "INIT" && userConsumer.time != null && timeslot.duration != null) {
                                        if (userConsumer.time!! >= timeslot.duration!!) {
                                            binding.buttonRequest.isVisible = true
                                        }
                                    }
                                }
                            }
                    }
            }

        binding.messageListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.messageListRecycler.adapter = messageListAdapter

        vm.getMessages(jobID).observe(viewLifecycleOwner) {
            messageListAdapter.setMessages(it.sortedBy { a -> a.sentAt }.toMutableList())
        }

        binding.buttonSend.setOnClickListener {
            vm.addMessage(
                jobID,
                binding.writeMessage.text.toString(),
                userID,
                System.currentTimeMillis()
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
        }

        binding.buttonAccept.setOnClickListener {
            if (userConsumer.time != null && timeslot.duration != null) {
                if (userConsumer.time!! >= timeslot.duration!!) {
                    // userconsumer does not have enough time
                    userConsumer.time = userConsumer.time!! - timeslot.duration!!
                    FirebaseFirestore.getInstance().collection("users")
                        .document(job.userConsumerID!!)
                        .set(userConsumer).addOnSuccessListener {

                            timeslot.booked = true
                            FirebaseFirestore.getInstance().collection("timeslots")
                                .document(job.timeslotID!!)
                                .set(timeslot).addOnSuccessListener {

                                    FirebaseFirestore.getInstance().collection("jobs")
                                        .document(jobID)
                                        .update("jobStatus", "ACCEPTED").addOnSuccessListener {
                                            binding.buttonAccept.visibility = View.GONE
                                            binding.buttonReject.visibility = View.GONE
                                        }
                                }
                        }
                } else {
                    Log.d("test", "there is a problem with the time and durations2")
                    // TODO Error message
                }

            } else {
                Log.d("test", "there is a problem with the time and durations1")
                // TODO Error message
            }


            // TODO error handling

//            FirebaseFirestore.getInstance().collection("users")
//                .document(job.userConsumerID!!).get()
//                .addOnSuccessListener { userIt ->
//
//                    FirebaseFirestore.getInstance().collection("timeslots")
//                        .document(job.timeslotID!!).get()
//                        .addOnSuccessListener { ts ->
//                            val timeRequired = ts.get("duration").toString().toInt()
//                            val time = userIt.get("time").toString().toInt()
//                            Log.d("test", "QUI $time vs $timeRequired")
//                            if (time >= timeRequired) {
//                                val data: MutableMap<String, Any> = mutableMapOf()
//                                data["available"] = false
//                                FirebaseFirestore.getInstance().collection("timeslots")
//                                    .document(job.timeslotID!!)
//                                    .update(data)
//                            } else {
//                                Toast.makeText(
//                                    context,
//                                    "You don't have enough time to spend!",
//                                    Toast.LENGTH_LONG
//                                ).show()
//                            }
//                        }
//                }
        }

        binding.buttonReject.setOnClickListener {
            FirebaseFirestore.getInstance().collection("jobs").document(jobID).update("jobStatus", "REJECTED")
                .addOnSuccessListener {
                    binding.buttonAccept.visibility = View.GONE
                    binding.buttonReject.visibility = View.GONE
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}