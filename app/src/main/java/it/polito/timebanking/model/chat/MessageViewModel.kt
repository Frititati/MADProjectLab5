package it.polito.timebanking.model.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    private var _firebase: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getMessages(chatID: String): LiveData<List<MessageData>> {
        val messages = MutableLiveData<List<MessageData>>()

        _firebase.collection("messages").whereEqualTo("chatID", chatID)
            .addSnapshotListener { r, e ->
                if (r != null) {
                    messages.value = if (e != null)
                        emptyList()
                    else r.mapNotNull { it.toMessageData() }
                }
            }
        return messages
    }
}