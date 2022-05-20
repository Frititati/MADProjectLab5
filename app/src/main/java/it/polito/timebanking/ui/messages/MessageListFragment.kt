package it.polito.timebanking.ui.messages

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R


class MessageListFragment : Fragment() {
    private lateinit var listener:NavBarUpdater

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        listener = context as NavBarUpdater
        listener.onFragmentInteraction("Chat with " + arguments?.getString("user"))
        return inflater.inflate(R.layout.message_sent, container, false)
    }
}