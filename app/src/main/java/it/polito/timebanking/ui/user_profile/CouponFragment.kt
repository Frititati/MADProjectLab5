package it.polito.timebanking.ui.user_profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.databinding.FragmentCouponBinding
import it.polito.timebanking.model.coupon.CouponData
import it.polito.timebanking.model.coupon.CouponViewModel
import it.polito.timebanking.model.coupon.toCouponData
import it.polito.timebanking.model.transaction.TransactionData

class CouponFragment : Fragment() {
    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!
    private val couponUsedAdapter = CouponUsedAdapter()
    private val firebaseUserID = FirebaseAuth.getInstance().uid!!
    private val vm by viewModels<CouponViewModel>()
    private var userCoupons = mutableListOf<Pair<String, CouponData>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.usedCouponRecycler.layoutManager = LinearLayoutManager(context)
        binding.usedCouponRecycler.adapter = couponUsedAdapter

        vm.getCouponsFromUser(firebaseUserID).observe(viewLifecycleOwner) {
            userCoupons = it.toMutableList()
            couponUsedAdapter.setCoupons(it.toMutableList())
        }

        binding.addCoupon.setOnClickListener {
            FirebaseFirestore.getInstance().collection("coupons").whereEqualTo("name", binding.couponSubmit.text.toString().trim().uppercase()).get().addOnSuccessListener {
                if (it.isEmpty) {
                    Snackbar.make(view, "No Coupon with that name", 1500).show()
                }
                else {
                    val coupon = it.first()
                    if (userCoupons.any { uC -> uC.first == coupon.id }) {
                        Snackbar.make(view, "Coupon Already Used", 1500).show()
                    }
                    else {
                        FirebaseFirestore.getInstance().collection("users").document(firebaseUserID)
                            .update("time", FieldValue.increment(coupon.toCouponData().value), "usedCoupons", FieldValue.arrayUnion(coupon.id)).addOnSuccessListener {
                                addTransaction(String.format("Coupon %s", coupon.toCouponData().name), firebaseUserID, coupon.toCouponData().value)
                                Snackbar.make(view, "Coupon Accepted", 1500).show()
                            }.addOnFailureListener { e -> Log.w("warn", "Error with users $e") }
                    }
                }
            binding.couponSubmit.text.clear()
            }.addOnFailureListener { e -> Log.w("warn", "Error with coupons $e") }
        }
    }

    private fun addTransaction(jobTitle: String, userID: String, time: Long) {
        val transaction = TransactionData(
            jobTitle,
            userID,
            time,
            true,
            System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("transactions").add(transaction).addOnSuccessListener {
        }.addOnFailureListener { e -> Log.w("warn", "Error with transactions $e") }
    }
}