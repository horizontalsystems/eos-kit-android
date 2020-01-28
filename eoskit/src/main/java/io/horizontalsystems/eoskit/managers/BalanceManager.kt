package io.horizontalsystems.eoskit.managers

import io.horizontalsystems.eoskit.EosKit
import io.horizontalsystems.eoskit.core.IStorage
import io.horizontalsystems.eoskit.core.Token
import io.horizontalsystems.eoskit.models.Balance
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

class BalanceManager(private val storage: IStorage, private val rpcProvider: EosioJavaRpcProviderImpl) {

    interface Listener {
        fun onSyncBalance(balance: Balance)
        fun onSyncBalanceFail(token: String)
    }

    var listener: Listener? = null

    private val disposables = CompositeDisposable()

    fun getBalance(symbol: String): Balance? {
        return storage.getBalance(symbol)
    }

    fun sync(account: String, token: Token) {
        if (token.syncState == EosKit.SyncState.Syncing) {
            return
        }

        token.syncState = EosKit.SyncState.Syncing

        Single.fromCallable { getBalances(account, token) }
                .subscribeOn(Schedulers.io())
                .doOnError { }
                .subscribe({ }, {
                    it?.printStackTrace()
                    listener?.onSyncBalanceFail(token.token)
                })
                .let { disposables.add(it) }
    }

    fun stop() {
        disposables.dispose()
    }

    private fun getBalances(account: String, token: Token): List<Balance> {
        val reqJson = JSONObject().apply {
            put("code", token.token)
            put("account", account)
        }

        val reqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), reqJson.toString())
        val resJson = rpcProvider.getCurrencyBalance(reqBody)

        val balances = mutableListOf<Balance>()
        val jsonArray = JSONArray(resJson)

        for (i in 0 until jsonArray.length()) {
            val element = jsonArray.getString(i).split(" ")
            balances.add(Balance(element[1], BigDecimal(element[0]), token.token))
        }

        if (balances.isEmpty()) {
            listener?.onSyncBalance(Balance(token.symbol, BigDecimal.ZERO, token.token))
        } else {
            storage.setBalances(balances)

            balances.forEach { listener?.onSyncBalance(it) }
        }

        return balances
    }

}
