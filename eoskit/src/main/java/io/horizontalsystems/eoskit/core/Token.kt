package io.horizontalsystems.eoskit.core

import io.horizontalsystems.eoskit.EosKit.SyncState
import io.horizontalsystems.eoskit.models.Transaction
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

class Token(val token: String, val symbol: String) {

    val syncStateFlowable: Flowable<SyncState>
        get() = syncStateSubject.toFlowable(BackpressureStrategy.BUFFER)

    val balanceFlowable: Flowable<BigDecimal>
        get() = balanceSubject.toFlowable(BackpressureStrategy.BUFFER)

    val transactionsFlowable: Flowable<List<Transaction>>
        get() = transactionsSubject.toFlowable(BackpressureStrategy.BUFFER)

    var syncState: SyncState = SyncState.NotSynced(NotStartedState())
        set(value) {
            if (field != value) {
                field = value
                syncStateSubject.onNext(syncState)
            }
        }

    var balance: BigDecimal = BigDecimal(0)
        set(value) {
            field = value
            balanceSubject.onNext(balance)
        }

    val transactionsSubject = PublishSubject.create<List<Transaction>>()
    private val balanceSubject = PublishSubject.create<BigDecimal>()
    private val syncStateSubject = PublishSubject.create<SyncState>()
}
