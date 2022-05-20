package it.polito.timebanking.ui.messages

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.timebanking.databinding.FragmentChatListBinding
import it.polito.timebanking.model.chat.ChatData
import it.polito.timebanking.model.chat.ChatViewModel
import it.polito.timebanking.model.chat.toChatData

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
        binding.chatListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.chatListRecycler.adapter = chatListAdapter

        chatListAdapter.clear()
        val me = FirebaseAuth.getInstance().currentUser!!.uid
        vm.getChat(me).observe(viewLifecycleOwner) {
            it.forEach { chat ->
                val other = if (chat.second.users!!.second == me) chat.second.users!!.first
                else
                    chat.second.users!!.second
                FirebaseFirestore.getInstance().collection("users")
                    .document(other).get()
                    .addOnSuccessListener { user ->
                        if (user != null) {
                            Log.d("test", "WE FOUND ${user.get("fullName").toString()}")
                            Firebase.storage.getReferenceFromUrl("gs://madproject-3381c.appspot.com/user_profile_picture/${other}.png")
                                .getBytes(1024 * 1024)
                                .addOnSuccessListener { pic ->
                                    chatListAdapter.addChat(Pair(user.get("fullName").toString(),pic))
                                }
                        }
                    }
            }
        }
    }

}