package it.polito.timebanking.model.chat

import com.google.firebase.firestore.DocumentSnapshot

data class ChatData(
    var userList: List<String>?,
    var messages: List<MessageData>?,
)

fun DocumentSnapshot.toChatData(): ChatData{
    return ChatData(
        this.get("users") as List<String>?,
        this.get("messages") as List<MessageData>?
        )
}