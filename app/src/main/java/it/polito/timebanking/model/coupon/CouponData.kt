package it.polito.timebanking.model.coupon

import com.google.firebase.firestore.DocumentSnapshot

data class CouponData(
    var name: String,
    var value: Long
)

fun DocumentSnapshot.toCouponData(): CouponData {
    return CouponData(
        this.getString("name") ?: "",
        this.getLong("value") ?: 0,
    )
}
