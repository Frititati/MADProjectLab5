package it.polito.timebanking.ui.user_profile

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebanking.R
import it.polito.timebanking.model.coupon.CouponData

class CouponUsedAdapter : RecyclerView.Adapter<CouponUsedAdapter.CouponListViewHolder>() {
    private var allCoupons = mutableListOf<Pair<String, CouponData>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponListViewHolder {
        return CouponListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_used_coupon, parent, false))
    }

    override fun onBindViewHolder(holder: CouponListViewHolder, position: Int) {
        holder.bind(allCoupons[position].second)
    }

    override fun getItemCount() = allCoupons.size

    @SuppressLint("NotifyDataSetChanged")
    fun setCoupons(coupons: MutableList<Pair<String, CouponData>>) {
        allCoupons = coupons.toMutableList()
        notifyDataSetChanged()
    }

    class CouponListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name = v.findViewById<TextView>(R.id.couponName)!!
        val value = v.findViewById<TextView>(R.id.couponValue)!!

        fun bind(couponData: CouponData) {
            name.text = couponData.name
            value.text = timeFormatter(couponData.value)
        }

        private fun timeFormatter(time: Long): String {
            val h = if (time / 60L == 1L) "1 hour"
            else "${time / 60L} hours"
            val m = if (time % 60L == 1L) "1 min"
            else "${time % 60L} min"
            return if (h == "0 hours") m
            else "$h, $m"
        }
    }
}