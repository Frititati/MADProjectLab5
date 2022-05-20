package it.polito.timebanking.model.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class ChatViewModel(application: Application) : AndroidViewModel(application) {
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

    fun getChat(user:String): LiveData<List<Pair<String, ChatData>>> {
        val chats = MutableLiveData<List<Pair<String, ChatData>>>()

        _firebase.collection("chats").whereArrayContains("users",user)
            .addSnapshotListener { r, e ->
                if (r != null) {
                    chats.value = if (e != null)
                        emptyList()
                    else r.mapNotNull { Pair(it.id, it.toChatData()) }
                }
            }
        return chats
    }

    fun addMessage(chatID: String) {
        val data = mutableMapOf<String, Any>()
        data["hello"] = "salut"
        var mess: MutableList<Map<String, String>> = mutableListOf(mutableMapOf())
        _firebase.collection("chats").document(chatID).get().addOnSuccessListener {
            Log.d("test", "WE HAD ${it.get("messages")}")
        }
    }

}