package io.horizontalsystems.eoskit.sample

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.eoskit.models.Transaction

class TransactionsFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var transactionsRecyclerView: RecyclerView

    private val transactionsAdapter = TransactionsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)

            viewModel.transactions.observe(this, Observer { txs ->
                txs?.let { transactions ->
                    transactionsAdapter.items = transactions
                    transactionsAdapter.notifyDataSetChanged()
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transactions, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionsRecyclerView = view.findViewById(R.id.transactions)
        transactionsRecyclerView.adapter = transactionsAdapter
        transactionsRecyclerView.layoutManager = LinearLayoutManager(context)

        val ethFilter = view.findViewById<TextView>(R.id.eosFilter)
        val tokenFilter = view.findViewById<TextView>(R.id.tokenFilter)

        ethFilter.setOnClickListener {

        }

        tokenFilter.setOnClickListener {

        }
    }
}

class TransactionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items = listOf<Transaction>()

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderTransaction(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_transaction, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderTransaction -> holder.bind(items[position], itemCount - position)
        }
    }
}

class ViewHolderTransaction(private val containerView: View) : RecyclerView.ViewHolder(containerView) {
    private val summary = containerView.findViewById<TextView>(R.id.summary)!!

    fun bind(tx: Transaction, index: Int) {
        containerView.setBackgroundColor(if (index % 2 == 0)
            Color.parseColor("#dddddd") else
            Color.TRANSPARENT
        )

        val value = """
            - #$index
            - ID: ${tx.id}
            - From: ${tx.from}
            - To: ${tx.to}
            - Amount: ${tx.amount} ${tx.symbol}
            - Time: ${tx.date}
            - Memo: ${tx.memo}
            - Action Sequence: ${tx.actionSequence}
        """

        summary.text = value.trimIndent()
    }
}
