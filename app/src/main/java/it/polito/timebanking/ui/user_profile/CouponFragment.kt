package it.polito.timebanking.ui.user_profile

import android.os.Bundle
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

class CouponFragment : Fragment() {
    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!
    private val couponUsedAdapter = CouponUsedAdapter()
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

        vm.getCouponsFromUser(FirebaseAuth.getInstance().uid!!).observe(viewLifecycleOwner) {
            userCoupons = it.toMutableList()
            couponUsedAdapter.setCoupons(it.toMutableList())
        }

        binding.addCoupon.setOnClickListener {
            val tempCouponName = binding.couponSubmit.text.toString().toUpperCase()
            // find if the coupon actually exists
            FirebaseFirestore.getInstance().collection("coupons").whereEqualTo("name", tempCouponName).get().addOnSuccessListener {
                if (!it.isEmpty) {
                    // we have coupon with that name
                    val coupon = it.first().toCouponData()
                    if (!userCoupons.any { uC -> uC.first == it.first().id }) {
                        // coupon not used

                        val userEdit = mutableMapOf<String, Any>()
                        userEdit["time"] = FieldValue.increment(coupon.value)
                        userEdit["usedCoupons"] = FieldValue.arrayUnion(it.first().id)
                        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)
                            .update(userEdit).addOnSuccessListener {
                                Snackbar.make(view, "Coupon Accepted", 1500).show()
                                binding.couponSubmit.text.clear()
                            }
                    } else {
                        // already used the coupon
                        Snackbar.make(view, "Coupon Already Used", 1500).show()
                        binding.couponSubmit.text.clear()
                    }
                } else {
                    // we have no coupon with that name
                    Snackbar.make(view, "No Coupon with that name", 1500).show()
                    binding.couponSubmit.text.clear()
                }
            }
        }
    }
}