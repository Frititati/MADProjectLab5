package it.polito.timebanking.model.coupon

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class CouponViewModel(application: Application) : AndroidViewModel(application) {

    fun getCouponsFromUser(userID: String): LiveData<List<Pair<String, CouponData>>> {
        val coupons = MutableLiveData<List<Pair<String, CouponData>>>()

        FirebaseFirestore.getInstance().collection("users").document(userID).addSnapshotListener { r, _ ->
            if (r != null) {
                val couponList = mutableListOf<Pair<String, CouponData>>()
                (r.get("usedCoupons") as List<*>?)?.forEach {
                    FirebaseFirestore.getInstance().collection("coupons").document(it.toString()).addSnapshotListener { m, _ ->
                        if (m != null) {
                            couponList.add(Pair(m.id, m.toCouponData()))
                            coupons.value = couponList
                        }
                    }
                }
            }
        }
        return coupons
    }
}