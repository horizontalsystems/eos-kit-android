package io.horizontalsystems.eoskit

import android.content.Context
import io.horizontalsystems.eoskit.models.Balance
import io.horizontalsystems.eoskit.storage.KitDatabase
import io.horizontalsystems.eoskit.storage.Storage
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl

class EosKit(context: Context, host: String) : Blockchain.Listener {

    private val database = KitDatabase.create(context, "eos-kit-database")
    private val storage = Storage(database)
    private val blockchain = Blockchain(storage, EosioJavaRpcProviderImpl(host))

    private val balanceSubject = PublishSubject.create<Unit>()
    private val syncStateSubject = PublishSubject.create<SyncState>()

    val syncState: SyncState
        get() = blockchain.syncState

    val syncStateFlowable: Flowable<SyncState>
        get() = syncStateSubject.toFlowable(BackpressureStrategy.BUFFER)

    val balanceFlowable: Flowable<Unit>
        get() = balanceSubject.toFlowable(BackpressureStrategy.BUFFER)

    fun start() {
        blockchain.listener = this
        blockchain.start()
    }

    fun stop() {
        blockchain.stop()
    }

    fun refresh() {
        blockchain.refresh()
    }

    fun clear() {
    }

    fun getBalance(symbol: String): Balance? {
        return storage.getBalance(symbol)
    }

    // Blockchain Listener

    override fun onUpdateBalance(balances: List<Balance>) {
        storage.setBalances(balances)
        balanceSubject.onNext(Unit)
    }

    override fun onUpdateSyncState(syncState: SyncState) {
        syncStateSubject.onNext(syncState)
    }

    // SyncState

    sealed class SyncState {
        object Synced : SyncState()
        object NotSynced : SyncState()
        object Syncing : SyncState()
    }
}
