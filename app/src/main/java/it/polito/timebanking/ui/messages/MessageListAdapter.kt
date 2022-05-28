package it.polito.timebanking.ui.messages

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import it.polito.timebanking.R
import it.polito.timebanking.model.chat.MessageData
import java.text.SimpleDateFormat
import java.util.*


class MessageListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mSent = 1
    private val mReceived = 2
    private val mSystem = 3
    private var messageList = mutableListOf<MessageData>()
    private val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            mSent -> {
                SentMessageHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_message_sent, parent, false))
            }
            mReceived -> {
                ReceivedMessageHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_message_received, parent, false))
            }
            else -> {
                SystemMessageHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_message_system, parent, false))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message: MessageData = messageList[position]
        if (message.system) return mSystem
        return if (message.senderID == firebaseUser) {
            mSent
        } else {
            mReceived
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: MessageData = messageList[position]

        when (holder.itemViewType) {
            mSent -> (holder as SentMessageHolder).bind(message)
            mReceived -> (holder as ReceivedMessageHolder).bind(message)
            mSystem -> (holder as SystemMessageHolder).bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(list: MutableList<MessageData>) {
        messageList = list
        notifyDataSetChanged()
    }

    private class SentMessageHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: MessageData) {
            itemView.findViewById<TextView>(R.id.message_text)!!.text = message.message
            itemView.findViewById<TextView>(R.id.message_hour)!!.text = timeFormatter(message.sentAt)
        }

        private fun timeFormatter(time: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            return SimpleDateFormat("hh:mm a", Locale.ITALIAN).format(calendar.time)
        }
    }

    private class ReceivedMessageHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: MessageData) {
            itemView.findViewById<TextView>(R.id.message_text)!!.text = message.message
            itemView.findViewById<TextView>(R.id.message_hour)!!.text = timeFormatter(message.sentAt)
        }

        private fun timeFormatter(time: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            return SimpleDateFormat("hh:mm a", Locale.ITALIAN).format(calendar.time)
        }
    }

    private class SystemMessageHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: MessageData) {
            itemView.findViewById<TextView>(R.id.message_text)!!.text = message.message
        }
    }

}


