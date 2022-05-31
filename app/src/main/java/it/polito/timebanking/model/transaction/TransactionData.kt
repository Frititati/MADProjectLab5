package it.polito.timebanking.model.transaction

import com.google.firebase.firestore.DocumentSnapshot

data class TransactionData(
    var jobTitle: String,
    var userID: String,
    var time: Long,
    var isCoupon: Boolean,
    var transactionTime: Long
)

fun DocumentSnapshot.toTransactionData(): TransactionData {
    return TransactionData(
        this.getString("jobTitle") ?: "",
        this.getString("userID") ?: "",
        this.getLong("time") ?: 0L,
        this.getBoolean("isCoupon") ?: false,
        this.getLong("transactionTime") ?: 0
    )
}
