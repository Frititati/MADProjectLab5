package it.polito.timebanking.model.chat

import com.google.firebase.firestore.DocumentSnapshot

data class MessageData(
    var senderID: String?,
    var receiverID: String?,
    var message:String?,
    /*var sentAt: Long?,
    var receivedAt: Long?,
    var status: Int?,
    var seenAt: Long?*/
)

fun DocumentSnapshot.toMessageData(): MessageData{
    return MessageData(
        this.get("senderID") as String?,
        this.get("receiverID") as String?,
        this.get("message").toString(),
       /* this.get("sentAt") as Long?,
        this.get("receivedAt") as Long?,
        this.get("status").toString().toIntOrNull(),
        this.get("seenAt") as Long?,*/
    )
}
