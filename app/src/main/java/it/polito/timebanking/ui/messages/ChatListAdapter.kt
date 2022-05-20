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
import it.polito.timebanking.R

class ChatListAdapter : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {
    private var allChats: MutableList<Pair<String,ByteArray>> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatListViewHolder {
        return ChatListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.widget_chat_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(allChats[position].first,allChats[position].second)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setChats(chats: MutableList<Pair<String,ByteArray>>) {
        allChats = chats
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun addChat(chat: Pair<String,ByteArray>) {
        allChats.add(chat)
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun clear(){
        allChats.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return allChats.size
    }

    class ChatListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val u = v.findViewById<TextView>(R.id.chatMember)
        private val image = v.findViewById<ImageView>(R.id.userImageOnChat)
        private val rootView = v
        fun bind(user: String, pic: ByteArray) {
            u.text = user
            image.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    pic,
                    0,
                    pic.size
                )
            )
            u.setOnClickListener {
                rootView.findNavController()
                    .navigate(R.id.chatList_to_chat, bundleOf("user" to user))
            }
        }
    }
}
