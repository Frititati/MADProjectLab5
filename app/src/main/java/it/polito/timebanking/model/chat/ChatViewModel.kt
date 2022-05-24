package it.polito.timebanking.model.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private var _firebase: FirebaseFirestore = FirebaseFirestore.getInstance()

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
}