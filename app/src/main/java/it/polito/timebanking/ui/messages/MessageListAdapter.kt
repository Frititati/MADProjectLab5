
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
    private var messageList: MutableList<MessageData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == mSent)
            SentMessageHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_message_sent, parent, false))
         else
            ReceivedMessageHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_message_received, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        val message: MessageData = messageList[position]
        return if (message.senderID == FirebaseAuth.getInstance().currentUser!!.uid) {
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
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(list:MutableList<MessageData>){
        messageList = list
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        messageList.clear()
        notifyDataSetChanged()
    }
    private class SentMessageHolder constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
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

    private class ReceivedMessageHolder constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
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



}


