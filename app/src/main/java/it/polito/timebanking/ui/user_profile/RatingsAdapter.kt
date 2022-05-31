package it.polito.timebanking.ui.user_profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.timebanking.R
import it.polito.timebanking.model.rating.RateData
import it.polito.timebanking.model.profile.toUserProfileData

class RatingsAdapter : RecyclerView.Adapter<RatingsAdapter.RatingsViewHolder>() {

    private var allRates = mutableListOf<Pair<String, RateData>>()
    private val firebaseUserID = FirebaseAuth.getInstance().uid!!

    class RatingsViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val rootView = v
        private val score = v.findViewById<TextView>(R.id.rate)
        private val image = v.findViewById<ImageView>(R.id.userImageOnRate)
        private val name = v.findViewById<TextView>(R.id.ratePerson)
        private val cardView = v.findViewById<CardView>(R.id.rating_card_view)
        private val firebaseUserID = FirebaseAuth.getInstance().uid!!

        fun bind(rating: RateData, context: Context) {
            score.text = rating.score.toString()
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, chooseColorForRate(rating.score)))
            val userIDToShow = if (rating.receiverID == firebaseUserID) rating.senderID else rating.receiverID
            FirebaseFirestore.getInstance().collection("users").document(userIDToShow).get().addOnSuccessListener {
                name.text = it.toUserProfileData().fullName
            }.addOnFailureListener { e -> Log.w("warn", "Error with users $e") }
            Firebase.storage.getReferenceFromUrl(String.format(context.getString(R.string.firebaseUserPic, userIDToShow))).getBytes(1024 * 1024).addOnSuccessListener { pic ->
                image.setImageBitmap(BitmapFactory.decodeByteArray(pic, 0, pic.size))
            }.addOnFailureListener { e -> Log.w("warn", "Error with users $e") }

            rootView.setOnClickListener {
                val dialog = AlertDialog.Builder(context)
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null)
                dialog.setTitle("Rating details")
                dialogView.findViewById<TextView>(R.id.comment).text = rating.comment.ifEmpty { "No feedback given" }
                dialogView.findViewById<TextView>(R.id.job).text = rating.jobName
                dialog.setView(dialogView)
                dialog.create().show()
            }
        }

        private fun chooseColorForRate(score: Double): Int {
            return if (score < 1.0) R.color.Ruby_Red
            else if (score < 2.0) R.color.Bean_Red
            else if (score < 3.0) R.color.Bee_Yellow
            else if (score < 4.0) R.color.Sun_Yellow
            else if (score < 5.0) R.color.Stoplight_Go_Green
            else if (score == 5.0) R.color.Yellow_Green
            else R.color.ColorBlue
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingsViewHolder {
        return RatingsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_ratings, parent, false))
    }

    override fun onBindViewHolder(holder: RatingsViewHolder, position: Int) {
        holder.bind(allRates[position].second, holder.itemView.context)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRatings(ratings: MutableList<Pair<String, RateData>>, received: Boolean): Int {
        allRates = if (received) ratings.filter { it.second.receiverID == firebaseUserID }.toMutableList()
        else ratings.filter { it.second.senderID == firebaseUserID }.toMutableList()
        notifyDataSetChanged()
        return allRates.size
    }

    override fun getItemCount() = allRates.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortByRate(higher: Boolean) {
        allRates = allRates.sortedBy { it.second.score }.toMutableList()
        if (higher) allRates.reverse()
        notifyDataSetChanged()
    }
}