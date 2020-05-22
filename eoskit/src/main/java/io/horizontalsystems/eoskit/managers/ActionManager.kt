package io.horizontalsystems.eoskit.managers

import io.horizontalsystems.eoskit.core.IStorage
import io.horizontalsystems.eoskit.core.Token
import io.horizontalsystems.eoskit.models.Action
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import one.block.eosiojava.error.rpcProvider.RpcProviderError
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ActionManager(private val storage: IStorage, private val rpcProvider: EosioJavaRpcProviderImpl) {

    private val MAX_RECORD_FETCH_COUNT: Short = 100

    interface Listener {
        fun onSyncActions(actions: List<Action>)
        fun onChangeLastIrreversibleBlock(height: Int)
    }

    var listener: Listener? = null
    val irreversibleBlockHeight: Int?
        get() = storage.lastIrreversibleBlock?.height

    private val disposables = CompositeDisposable()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun sync(account: String) {
        Single.fromCallable { getActions(account, storage.lastAction?.sequence ?: -1) }
                .subscribeOn(Schedulers.io())
                .doOnError { it?.printStackTrace() }
                .subscribe({ }, { it?.printStackTrace() })
                .let { disposables.add(it) }
    }

    fun validateAccount(account: String) {
        val reqJson = JSONObject().apply {
            put("account_name", account)
        }

        val reqBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            reqJson.toString()
        )

        try {
            rpcProvider.getAccount(reqBody)
        } catch (e: RpcProviderError) {
            var errorMessage = e.message
            e.cause?.message?.let {
                errorMessage += "\n$it"
            }
            throw Throwable(errorMessage)
        }
    }

    fun stop() {
        disposables.dispose()
    }

    fun getActions(account: String, token: Token, fromSequence: Int? = null, limit: Int? = null): Single<List<Action>> {
        return Single.just(storage.getActions(token.token, token.symbol, account, fromSequence, limit))
    }

    private fun getActions(account: String, position: Int) {
        val reqJson = JSONObject().apply {
            put("pos", position + 1)
            put("offset", MAX_RECORD_FETCH_COUNT)
            put("account_name", account)
        }

        val reqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), reqJson.toString())
        val resJson = rpcProvider.getActions(reqBody)
        val results = JSONObject(resJson)

        val actJson = results.getJSONArray("actions")
        updateLastIrreversibleBlock(results.getInt("last_irreversible_block"))

        val (actionLastSequence, actions) = parse(actJson)
        if (actions.isNotEmpty()) {
            storage.setActions(actions)
            val filteredActions = actions.filter { it.receiver == account && it.name == "transfer" }

            listener?.onSyncActions(filteredActions)

            getActions(account, actionLastSequence)
        }
    }

    private fun parse(actions: JSONArray): Pair<Int,List<Action>> {
        val transactions = mutableListOf<Action>()
        var actionLastSequence: Int = 0

        for (i in 0 until actions.length()) {
            try {
                val action = actions.getJSONObject(i)
                actionLastSequence = action.getInt("account_action_seq")
                val trace = action.getJSONObject("action_trace")

                val receipt = trace.getJSONObject("receipt")
                val receiver = receipt.getString("receiver")

                val transactionId = trace.getString("trx_id")
                val blockNumber = trace.getInt("block_num")
                val timestamp = dateFormat.parse(trace.getString("block_time")).time

                val act = trace.getJSONObject("act")
                val type = act.optString("name")
                val token = act.optString("account")
                val data = act.getJSONObject("data")
                val from = data.optString("from")
                val to = data.optString("to")
                val memo = data.optString("memo")

                var amount: String? = null
                var symbol: String? = null
                val quantity = data.optString("quantity").split(" ")
                if (quantity.size >= 2) {
                    amount = quantity[0]
                    symbol = quantity[1]
                }

                transactions.add(Action(
                        sequence = actionLastSequence,
                        name = type,
                        transactionId = transactionId,
                        blockNumber = blockNumber,
                        blockTime = timestamp,
                        account = token,

                        receiver = receiver,
                        from = from,
                        to = to,
                        amount = amount,
                        symbol = symbol,
                        memo = memo
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Pair(actionLastSequence, transactions)
    }

    private fun updateLastIrreversibleBlock(height: Int) {
        storage.setIrreversibleBlock(height)
        listener?.onChangeLastIrreversibleBlock(height)
    }
}
