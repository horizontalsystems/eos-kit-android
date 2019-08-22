package io.horizontalsystems.eoskit.sample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import io.horizontalsystems.eoskit.core.exceptions.BackendError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.math.RoundingMode

class SendTransactionFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private lateinit var sendButton: Button
    private lateinit var sendAmount: EditText
    private lateinit var sendMemo: EditText
    private lateinit var sendAddress: EditText

    private var adapter: EosAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
            adapter = viewModel.adapters.last()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_send_receive, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendAddress = view.findViewById(R.id.sendUsername)
        sendAmount = view.findViewById(R.id.sendAmount)
        sendMemo = view.findViewById(R.id.sendMemo)
        sendButton = view.findViewById(R.id.sendButton)
        sendButton.setOnClickListener {
            when {
                sendAddress.text.isEmpty() -> sendAddress.error = "Send address cannot be blank"
                sendAmount.text.isEmpty() -> sendAmount.error = "Send amount cannot be blank"
                else -> send()
            }
        }
    }

    private fun send() {
        val tokenPrecision = 9 //EOSDT token precision is 9, EOS precision is 4
        adapter?.let { adapter ->
            val amount = sendAmount.text.toString().toBigDecimal()
            val scaledAmount = amount.setScale(tokenPrecision, RoundingMode.DOWN)
            adapter.send(sendAddress.text.toString(), scaledAmount, sendMemo.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { e -> e.printStackTrace() }
                    .subscribe({
                        messageSent(null)
                    }, {
                        messageSent(it)
                    })

            sendAddress.text = null
            sendAmount.text = null
            sendMemo.text = null
        }
    }

    private fun messageSent(sendError: Throwable?) {
        val message = if (sendError != null) {
            if (sendError is BackendError) {
                parseBackendError(sendError)
                "${sendError.message} - ${sendError.code} - ${sendError.detail}"
            } else {
                sendError.localizedMessage
            }
        } else {
            " Successfully sent!"
        }

        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        }
    }

    private fun parseBackendError(sendError: BackendError) {
        when(sendError) {
            is BackendError.BalanceOverdrawnError -> Log.e("SendTxFrag", "balance overdrawn")
            is BackendError.AccountNotExistError -> Log.e("SendTxFrag", "account does not exist")
            is BackendError.SymbolPrecisionMismatchError -> Log.e("SendTxFrag", "symbol precision mismatch")
            is BackendError.InsufficientRamError -> Log.e("SendTxFrag", "insufficient ram")
        }
    }

}
