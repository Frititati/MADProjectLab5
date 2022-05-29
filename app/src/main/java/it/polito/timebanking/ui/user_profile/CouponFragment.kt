package it.polito.timebanking.ui.user_profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.polito.timebanking.databinding.FragmentCouponBinding
import it.polito.timebanking.model.coupon.CouponData
import it.polito.timebanking.model.coupon.CouponViewModel

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
            val tempCoupon = binding.couponSubmit.text.toString()
            if (userCoupons.any { it.first == tempCoupon }) {
                // already used the coupon
            } else {
                // can use coupon
                // check if coupon exists
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!).update("usedCoupons", FieldValue.arrayUnion(tempCoupon)).addOnSuccessListener {

                }
            }

        }
    }
}