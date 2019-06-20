package io.horizontalsystems.eoskit

import io.horizontalsystems.eoskit.EosKit.SyncState
import io.horizontalsystems.eoskit.core.IBlockchain
import io.horizontalsystems.eoskit.core.IStorage
import io.horizontalsystems.eoskit.models.Balance
import io.horizontalsystems.eoskit.models.Transaction
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

class Blockchain(private val storage: IStorage, private val rpcProvider: EosioJavaRpcProviderImpl) : IBlockchain {

    private val disposables = CompositeDisposable()

    interface Listener {
        fun onUpdateBalance(balances: List<Balance>)
        fun onUpdateSyncState(syncState: SyncState)
    }

    override var listener: Listener? = null

    override val balance: BigDecimal?
        get() = TODO("not implemented")

    override var syncState: SyncState = SyncState.NotSynced
        private set(value) {
            if (field != value) {
                field = value
                listener?.onUpdateSyncState(value)
            }
        }

    override fun start() {
        val json = JSONObject().apply {
            put("code", "eosio.token")
            put("account", "eosio")
        }

        Single.create<List<Balance>> { emitter ->
            try {
                val reqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
                val resJSON = rpcProvider.getCurrencyBalance(reqBody)

                val balances = mutableListOf<Balance>()
                val jsonArray = JSONArray(resJSON)

                for (i in 0 until jsonArray.length()) {
                    val element = jsonArray.getString(i).split(" ")
                    balances.add(Balance(element[1], BigDecimal(element[0])))
                }

                emitter.onSuccess(balances)
            } catch (e: Exception) {
                emitter.onError(Error("Failed to fetch account balance: ${e.message}"))
            }
        }.subscribeOn(Schedulers.io())
                .subscribe({ balances ->
                    listener?.onUpdateBalance(balances)
                    syncState = SyncState.Synced
                }, {
                    it?.printStackTrace()
                    syncState = SyncState.NotSynced
                })
                .let { disposables.add(it) }
    }

    override fun refresh() {

    }

    override fun stop() {
    }

    override fun send(): Single<Transaction> {
        TODO("not implemented")
    }
}
