package io.horizontalsystems.eoskit

import android.content.Context
import io.horizontalsystems.eoskit.managers.ActionManager
import io.horizontalsystems.eoskit.managers.BalanceManager
import io.horizontalsystems.eoskit.models.Action
import io.horizontalsystems.eoskit.models.Balance
import io.horizontalsystems.eoskit.storage.KitDatabase
import io.horizontalsystems.eoskit.storage.Storage
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl

class EosKit(context: Context, host: String, account: String) : BalanceManager.Listener, ActionManager.Listener {

    private val rpcProvider = EosioJavaRpcProviderImpl(host)

    private val database = KitDatabase.create(context, "eos-kit-database-$account")
    private val storage = Storage(database)
    private val balanceManager = BalanceManager(account, storage, rpcProvider)
    private val actionManager = ActionManager(account, storage, rpcProvider)

    private val balanceSubject = PublishSubject.create<Unit>()
    private val transactionsSubject = PublishSubject.create<Unit>()
    private val syncStateSubject = PublishSubject.create<SyncState>()

    val syncState: SyncState
        get() = SyncState.Synced

    val syncStateFlowable: Flowable<SyncState>
        get() = syncStateSubject.toFlowable(BackpressureStrategy.BUFFER)

    val balanceFlowable: Flowable<Unit>
        get() = balanceSubject.toFlowable(BackpressureStrategy.BUFFER)

    val transactionsFlowable: Flowable<Unit>
        get() = transactionsSubject.toFlowable(BackpressureStrategy.BUFFER)

    fun start() {
        balanceManager.sync("eosio.token")
        balanceManager.listener = this
        actionManager.sync()
        actionManager.listener = this
    }

    fun stop() {
    }

    fun refresh() {
    }

    fun send(address: String, amount: String, memo: String) {
    }

    fun clear() {
    }

    fun getBalance(symbol: String): Balance? {
        return storage.getBalance(symbol)
    }

    fun transactions(token: String, fromSecuence: Int? = null, limit: Int? = null): List<Action> {
        return storage.getActions(token, fromSecuence, limit)
    }

    // Balance Manager Listener

    override fun onSyncBalance() {
        balanceSubject.onNext(Unit)
        syncStateSubject.onNext(SyncState.Synced)
    }

    override fun onSyncBalanceFail() {
        syncStateSubject.onNext(SyncState.NotSynced)
    }

    // Action Manager Listener

    override fun onSyncActions() {
        transactionsSubject.onNext(Unit)
    }

    // SyncState

    enum class SyncState {
        Synced,
        NotSynced,
        Syncing
    }
}
