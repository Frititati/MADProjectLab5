package it.polito.timebanking.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentMessagesBinding
import it.polito.timebanking.model.chat.MessageViewModel


class MessageListFragment : Fragment() {
    private lateinit var listener: NavBarUpdater
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<MessageViewModel>()
    private val messageListAdapter = MessageListAdapter()
    private lateinit var chatID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        listener = context as NavBarUpdater
        chatID = requireArguments().getString("chatID").toString()
        listener.setTitleWithSkill("Chat with " + requireArguments().getString("user"))
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.messageListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.messageListRecycler.adapter = messageListAdapter

        messageListAdapter.clear()

        vm.getMessages(chatID).observe(viewLifecycleOwner) {
            messageListAdapter.setMessages(it.toMutableList())
        }

        binding.buttonSend.setOnClickListener {
            vm.addMessage(chatID,binding.writeMessage.text.toString(),FirebaseAuth.getInstance().currentUser!!.uid)
            binding.writeMessage.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}