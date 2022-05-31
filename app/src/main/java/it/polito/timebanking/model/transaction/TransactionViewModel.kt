package it.polito.timebanking.model.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    fun getTransactions(userID: String): LiveData<List<TransactionData>> {
        val transactions = MutableLiveData<List<TransactionData>>()
        FirebaseFirestore.getInstance().collection("transactions").whereEqualTo("userID", userID).addSnapshotListener { r, e ->
            if (r != null) {
                transactions.value = if (e != null) emptyList()
                else r.map { it.toTransactionData() }
            }
        }
        return transactions
    }
}