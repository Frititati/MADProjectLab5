package it.polito.timebanking.model.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MessageViewModel(application: Application) : AndroidViewModel(application) {

    fun getMessages(chatID: String): LiveData<List<MessageData>> {
        val messages = MutableLiveData<List<MessageData>>()

        FirebaseFirestore.getInstance().collection("chats").document(chatID)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    val messagesIDList = r.get("messagesList") as List<String>
                    val messagesList: MutableList<MessageData> = mutableListOf()
                    messagesIDList.forEach {
                        FirebaseFirestore.getInstance().collection("messages").document(it)
                            .addSnapshotListener { m, _ ->
                                messagesList.add(m!!.toMessageData())
                                messages.value = messagesList
                            }
                    }
                }
            }
        return messages
    }

    fun addMessage(chatID: String, message: String, senderID: String) {
        val data = mutableMapOf<String, Any>()
        data["message"] = message
        data["chatID"] = chatID
        data["senderID"] = senderID
        FirebaseFirestore.getInstance().collection("messages").add(data).addOnSuccessListener {
            FirebaseFirestore.getInstance().collection("chats").document(chatID)
                .update("messagesList", FieldValue.arrayUnion(it.id))
        }

    }
}