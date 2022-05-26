package it.polito.timebanking.model.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.model.timeslot.toTimeslotData

class MessageViewModel(application: Application) : AndroidViewModel(application) {

    fun getMessages(chatID: String): LiveData<List<MessageData>> {
        val messages = MutableLiveData<List<MessageData>>()

        FirebaseFirestore.getInstance().collection("jobs").document(chatID)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    val messagesList: MutableList<MessageData> = mutableListOf()
                    (r.get("messagesList") as List<*>?)?.forEach {
                        FirebaseFirestore.getInstance().collection("messages")
                            .document(it.toString())
                            .addSnapshotListener { m, _ ->
                                if (m != null) {
                                    messagesList.add(messagesList.size, m.toMessageData())
                                    messages.value = messagesList
                                }
                            }
                    }
                }
            }

//        val messagesList: MutableList<MessageData> = mutableListOf()
//        FirebaseFirestore.getInstance().collection("messages").whereEqualTo("chatID", chatID)
//            .addSnapshotListener { m, e ->
//                if (m != null) {
//                    messages.value = if (e != null)
//                        emptyList()
//                    else m.map { it.toMessageData() }
//                }
//            }
        return messages
    }

    fun addMessage(senderID: String, jobID: String, message: String, time: Long, system: Boolean) {
        val messageData = MessageData(
            senderID,
            jobID,
            message,
            time,
            system
        )
        FirebaseFirestore.getInstance().collection("messages").add(messageData)
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("jobs").document(jobID)
                    .update(
                        "messagesList", FieldValue.arrayUnion(it.id),
                        "lastMessage", System.currentTimeMillis()
                    )
            }

    }
}