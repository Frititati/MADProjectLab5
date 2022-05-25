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
                        FirebaseFirestore.getInstance().collection("messages").document(it.toString())
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
//                    else m.mapNotNull { it.toMessageData() }
//                }
//            }
        return messages
    }

    fun addMessage(chatID: String, message: String, senderID: String, time: Long) {
        val data = mutableMapOf<String, Any>()
        data["message"] = message
        data["chatID"] = chatID
        data["senderID"] = senderID
        data["sentAt"] = time
        FirebaseFirestore.getInstance().collection("messages").add(data).addOnSuccessListener {
            FirebaseFirestore.getInstance().collection("jobs").document(chatID)
                .update("messagesList", FieldValue.arrayUnion(it.id),
                    "lastMessage",System.currentTimeMillis())
        }

    }
}