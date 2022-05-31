package it.polito.timebanking.ui.user_profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentTransactionsListBinding
import it.polito.timebanking.model.transaction.TransactionViewModel

class TransactionsListFragment : Fragment() {
    private var _binding: FragmentTransactionsListBinding? = null
    private val binding get() = _binding!!
    private var transactionListAdapter = TransactionListAdapter()
    private val transactionVM by viewModels<TransactionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransactionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        binding.transactionListRecycler.layoutManager = LinearLayoutManager(activity)
        binding.transactionListRecycler.adapter = transactionListAdapter


        transactionVM.getTransactions(FirebaseAuth.getInstance().uid!!).observe(viewLifecycleOwner){
            transactionListAdapter.setTransactions(it.toMutableList())
        }
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