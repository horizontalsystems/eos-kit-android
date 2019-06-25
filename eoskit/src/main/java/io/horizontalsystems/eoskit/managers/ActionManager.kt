package io.horizontalsystems.eoskit.managers

import io.horizontalsystems.eoskit.core.IStorage
import io.horizontalsystems.eoskit.core.Token
import io.horizontalsystems.eoskit.models.Action
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl
import org.json.JSONArray
import org.json.JSONObject

class ActionManager(private val account: String, private val storage: IStorage, private val rpcProvider: EosioJavaRpcProviderImpl) {

    interface Listener {
        fun onSyncActions(actions: List<Action>)
    }

    var listener: Listener? = null

    private val disposables = CompositeDisposable()

    fun sync() {
        Single.fromCallable { getActions(storage.lastAction?.sequence ?: -1) }
                .subscribeOn(Schedulers.io())
                .doOnError { it?.printStackTrace() }
                .subscribe({ }, { it?.printStackTrace() })
                .let { disposables.add(it) }
    }

    fun stop() {
        disposables.dispose()
    }

    fun getActions(token: Token, fromSequence: Int? = null, limit: Int? = null): Single<List<Action>> {
        return Single.create { it.onSuccess(storage.getActions(token.token, token.symbol, account, fromSequence, limit)) }
    }

    private fun getActions(position: Int) {
        val reqJson = JSONObject().apply {
            put("pos", position + 1)
            put("offset", 1000)
            put("account_name", account)
        }

        val reqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), reqJson.toString())
        val resJson = rpcProvider.getActions(reqBody)
        val actJson = JSONObject(resJson).getJSONArray("actions")
        val actions = parse(actJson)

        storage.setActions(actions)

        if (actJson.length() > 0) {
            getActions(actions[actions.size - 1].sequence)
        }

        listener?.onSyncActions(actions)
    }

    private fun parse(actions: JSONArray): List<Action> {
        val transactions = mutableListOf<Action>()

        for (i in 0 until actions.length()) {
            try {
                val action = actions.getJSONObject(i)
                val actionSequence = action.getInt("account_action_seq")
                val trace = action.getJSONObject("action_trace")

                val receipt = trace.getJSONObject("receipt")
                val receiver = receipt.getString("receiver")

                val transactionId = trace.getString("trx_id")
                val blockNumber = trace.getInt("block_num")
                val blockTime = trace.getString("block_time")

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
                        sequence = actionSequence,
                        name = type,
                        transactionId = transactionId,
                        blockNumber = blockNumber,
                        blockTime = blockTime,
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

        return transactions
    }
}
