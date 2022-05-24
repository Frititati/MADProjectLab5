package it.polito.timebanking.model.chat

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

data class MessageData(
    var senderID: String,
    var receiverID: String,
    var message: String,
    var sentAt: Long,
    /*var receivedAt: Long?,
    var status: Int,
    var seenAt: Long?*/
)

fun DocumentSnapshot.toMessageData(): MessageData{
    return MessageData(
        this.get("senderID").toString(),
        this.get("receiverID").toString(),
        this.get("message").toString(),
        this.get("sentAt").toString().toLong(),
        /* this.get("sentAt") as Long?,
         this.get("receivedAt") as Long?,
         this.get("status").toString().toIntOrNull(),
         this.get("seenAt") as Long?,*/
    )
}

fun QueryDocumentSnapshot.toMessageData(): MessageData{
    return MessageData(
        this.get("senderID").toString(),
        this.get("receiverID").toString(),
        this.get("message").toString(),
        this.get("sentAt").toString().toLong(),
        /* this.get("sentAt") as Long?,
         this.get("receivedAt") as Long?,
         this.get("status").toString().toIntOrNull(),
         this.get("seenAt") as Long?,*/
    )
}
