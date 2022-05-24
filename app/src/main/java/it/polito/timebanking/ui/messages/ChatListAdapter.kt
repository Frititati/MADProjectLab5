package it.polito.timebanking.ui.messages

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.timebanking.R
import it.polito.timebanking.model.chat.ChatData
import it.polito.timebanking.model.profile.toUserProfileData
import it.polito.timebanking.model.timeslot.toTimeslotData

class ChatListAdapter : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {
    private var allChats: MutableList<Pair<String, ChatData>> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatListViewHolder {
        return ChatListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.widget_chat_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(allChats[position].first, allChats[position].second)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setChat(chats: MutableList<Pair<String, ChatData>>) {
        allChats = chats
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return allChats.size
    }

    class ChatListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val userName = v.findViewById<TextView>(R.id.chatMember)
        private val timeslotTitle = v.findViewById<TextView>(R.id.timeslotTitle)
        private val image = v.findViewById<ImageView>(R.id.userImageOnChat)
        private val rootView = v
        fun bind(chatID: String, chat: ChatData) {
            val userID = FirebaseAuth.getInstance().currentUser!!.uid
            val otherUserID = if (chat.users!!.second == userID) chat.users!!.first
            else chat.users!!.second

            FirebaseFirestore.getInstance().collection("users")
                .document(otherUserID).get()
                .addOnSuccessListener { otherUser ->
                    userName.text = otherUser.toUserProfileData().fullName
                    if (otherUser != null) {
                        Firebase.storage.getReferenceFromUrl("gs://madproject-3381c.appspot.com/user_profile_picture/${otherUserID}.png")
                            .getBytes(1024 * 1024)
                            .addOnSuccessListener { pic ->
                                image.setImageBitmap(
                                    BitmapFactory.decodeByteArray(
                                        pic,
                                        0,
                                        pic.size
                                    )
                                )
                            }
                    }
                }
            
            FirebaseFirestore.getInstance().collection("timeslots")
                .document(chat.timeslotID).get()
                .addOnSuccessListener { timeslot ->
                    timeslotTitle.text = timeslot.toTimeslotData().title
                }

            rootView.setOnClickListener {
                rootView.findNavController()
                    .navigate(
                        R.id.chatList_to_chat,
                        bundleOf("user" to otherUserID, "chatID" to chatID)
                    )
            }
        }
    }
}
