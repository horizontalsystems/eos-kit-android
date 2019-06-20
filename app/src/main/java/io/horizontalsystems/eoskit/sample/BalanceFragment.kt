package io.horizontalsystems.eoskit.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.horizontalsystems.eoskit.EosKit.SyncState

class BalanceFragment : Fragment() {

    lateinit var viewModel: MainViewModel
    lateinit var balanceValue: TextView
    lateinit var balanceValueToken: TextView
    lateinit var kitStateValue: TextView
    lateinit var refreshButton: Button
    lateinit var clearButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)

            viewModel.balance.observe(this, Observer { balance ->
                balanceValue.text = (balance ?: 0).toString()
            })

            viewModel.balanceToken.observe(this, Observer { balance ->
                balanceValueToken.text = (balance ?: 0).toString()
            })

            viewModel.syncState.observe(this, Observer { kitState ->
                kitStateValue.text = when (kitState) {
                    is SyncState.Synced -> "Synced"
                    is SyncState.Syncing -> "Syncing"
                    is SyncState.NotSynced -> "NotSynced"
                    else -> "null"
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_balance, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        balanceValue = view.findViewById(R.id.balanceValue)
        balanceValueToken = view.findViewById(R.id.tokenBalanceValue)
        refreshButton = view.findViewById(R.id.buttonRefresh)
        clearButton = view.findViewById(R.id.buttonClear)
        kitStateValue = view.findViewById(R.id.kitStateValue)

        refreshButton.setOnClickListener {
            viewModel.refresh()
        }

        clearButton.setOnClickListener {
            viewModel.clear()
        }
    }
}
