package it.polito.timebanking.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentMessagesBinding
import it.polito.timebanking.model.chat.JobData
import it.polito.timebanking.model.chat.MessageViewModel
import it.polito.timebanking.model.chat.toJobData


class MessageListFragment : Fragment() {
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<MessageViewModel>()
    private val messageListAdapter = MessageListAdapter()
    private lateinit var jobID: String
    private lateinit var drawerListener: NavBarUpdater
    private lateinit var job : JobData

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

        FirebaseFirestore.getInstance().collection("jobs")
            .document(jobID).get().addOnSuccessListener {
                job = it.toJobData()
            }

        binding.messageListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.messageListRecycler.adapter = messageListAdapter

        messageListAdapter.clear()

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

        binding.buttonAccept.setOnClickListener {
            FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnSuccessListener { user ->
                    FirebaseFirestore.getInstance().collection("timeslots")
                        .document(job.timeslotID).get()
                        .addOnSuccessListener { ts ->
                            val timeRequired = ts.get("duration").toString().toInt()
                            val time = user.get("time").toString().toInt()
                            Log.d("test", "QUI $time vs $timeRequired")
                            if (time >= timeRequired) {
                                val data: MutableMap<String, Any> = mutableMapOf()
                                data["available"] = false
                                FirebaseFirestore.getInstance().collection("timeslots")
                                    .document(job.timeslotID)
                                    .update(data)
                            } else {
                                Toast.makeText(
                                    context,
                                    "You don't have enough time to spend!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}