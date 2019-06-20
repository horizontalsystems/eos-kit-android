package io.horizontalsystems.eoskit.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class SendReceiveFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private lateinit var sendButton: Button
    private lateinit var sendAmount: EditText
    private lateinit var sendMemo: EditText
    private lateinit var sendAddress: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
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
                else -> viewModel.send(
                        sendAddress.text.toString(),
                        sendAmount.text.toString(),
                        sendMemo.text.toString()
                )
            }
        }

        viewModel.sendStatus.observe(this, Observer { sendError ->
            val msg = if (sendError != null) {
                sendError.localizedMessage
            } else {
                " Successfully sent!"
            }

            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        })
    }

}
