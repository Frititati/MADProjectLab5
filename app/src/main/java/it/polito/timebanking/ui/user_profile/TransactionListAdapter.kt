package it.polito.timebanking.ui.user_profile

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.timebanking.R
import it.polito.timebanking.model.chat.JobData
import it.polito.timebanking.model.chat.toJobData
import it.polito.timebanking.model.profile.toUserProfileData
import it.polito.timebanking.model.timeslot.toTimeslotData
import it.polito.timebanking.model.transaction.TransactionData
import java.text.SimpleDateFormat
import java.util.*

class TransactionListAdapter : RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder>() {
    private var allTransactions = mutableListOf<TransactionData>()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): TransactionViewHolder {
        return TransactionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_transactions_list, parent, false))
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(allTransactions[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTransactions(chats: MutableList<TransactionData>) {
        allTransactions = chats.sortedByDescending { it.transactionTime }.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = allTransactions.size

    class TransactionViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val timeslotTitle = v.findViewById<TextView>(R.id.timeslotTitle)
        private val time = v.findViewById<TextView>(R.id.time)
        private val date = v.findViewById<TextView>(R.id.date)
        private val value = v.findViewById<TextView>(R.id.value)

        fun bind(transaction: TransactionData) {
            timeslotTitle.text = transaction.jobTitle
            time.text = timeFormatter(transaction.transactionTime)
            date.text = dateFormatter(transaction.transactionTime)
            if (transaction.time < 0){
                value.text = String.format("%s Lost", durationFormatter(-transaction.time))
            } else {
                value.text = String.format("%s Gained", durationFormatter(transaction.time))
            }
        }

        private fun timeFormatter(time: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            return SimpleDateFormat("hh:mm a", Locale.ITALIAN).format(calendar.time)
        }

        private fun dateFormatter(time: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            return SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).format(calendar.time)
        }

        private fun durationFormatter(time: Long): String {
            val h = if (time / 60L == 1L) "1 hour"
            else "${time / 60L} hours"
            val hEmpty = (time / 60L) == 0L
            val m = if (time % 60L == 1L) "1 min"
            else "${time % 60L} min"
            val mEmpty = (time.toInt() % 60) == 0
            Log.d("test", "'$h' $hEmpty and '$m' $mEmpty")
            return when {
                hEmpty -> m
                mEmpty -> h
                else -> "$h, $m"
            }
        }
    }
}