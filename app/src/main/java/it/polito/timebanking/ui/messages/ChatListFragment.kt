package it.polito.timebanking.ui.messages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentChatListBinding
import it.polito.timebanking.model.chat.ChatData
import it.polito.timebanking.model.chat.ChatViewModel

class ChatListFragment : Fragment() {
    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private var chatListAdapter = ChatListAdapter()
    private val vm by viewModels<ChatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(
            DrawerLayout.LOCK_MODE_UNLOCKED
        )

        binding.chatListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.chatListRecycler.adapter = chatListAdapter

//        chatListAdapter.clear()
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        vm.getChat(userID).observe(viewLifecycleOwner) {
            chatListAdapter.setChat(it as MutableList<Pair<String, ChatData>>)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}