package it.polito.timebanking.model.chat

import com.google.firebase.firestore.DocumentSnapshot

data class MessageData(
    var senderID: String,
    var chatID: String,
    var message: String,
    var sentAt: Long,
    var system: Boolean
)

fun DocumentSnapshot.toMessageData(): MessageData {
    return MessageData(
        this.getString("senderID") ?: "",
        this.getString("chatID") ?: "",
        this.getString("message") ?: "",
        this.getLong("sentAt") ?: 0L,
        this.getBoolean("system") ?: false
    )
}
