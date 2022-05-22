package it.polito.timebanking.model.chat

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

data class ChatData(
    var users: Pair<String, String>?,
    var lastMessage: Long?,
    var messageIDList: List<String>?
)

fun DocumentSnapshot.toChatData(): ChatData {
    return ChatData(
        pair(this.get("users") as List<String>?),
        this.get("lastMessage") as Long?,
        this.get("messagesList") as List<String>?,
    )
}

fun pair(input: List<String>?): Pair<String, String> {
    return Pair(input!![0], input[1])
}