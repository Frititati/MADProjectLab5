package it.polito.timebanking.model.chat

import com.google.firebase.firestore.DocumentSnapshot

data class ChatData(
    var users: List<*>,
    var lastMessage: Long,
    var messagesList: List<*>,
    var timeslotID: String
)

fun DocumentSnapshot.toChatData(): ChatData {
    return ChatData(
        this.get("users") as List<*>,
        this.get("lastMessage").toString().toLong(),
        this.get("messagesList") as List<*>,
        this.get("timeslotID").toString(),
    )
}

fun pair(input: List<*>): Pair<String, String> {
    return Pair(input[0].toString(), input[1].toString())
}