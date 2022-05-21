package it.polito.timebanking.ui.messages

import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentAdvertisementBinding
import it.polito.timebanking.databinding.FragmentMessagesBinding
import it.polito.timebanking.model.chat.MessageData
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
    ): View? {

        listener = context as NavBarUpdater
        chatID = requireArguments().getString("chatID").toString()
        listener.setTitleWithSkill("Chat with " + requireArguments().getString("user"))
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.messageListRecycler.adapter = messageListAdapter

        vm.getMessages(chatID).observe(viewLifecycleOwner) {
            messageListAdapter.setMessages(it.toMutableList())
        }
    }
}