package io.horizontalsystems.eoskit.core

import io.horizontalsystems.eoskit.EosKit.SyncState
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

class Token(val token: String, val symbol: String) {

    val syncStateFlowable: Flowable<SyncState>
        get() = syncStateSubject.toFlowable(BackpressureStrategy.BUFFER)

    val balanceFlowable: Flowable<BigDecimal>
        get() = balanceSubject.toFlowable(BackpressureStrategy.BUFFER)

    val transactionsFlowable: Flowable<Unit>
        get() = transactionsSubject.toFlowable(BackpressureStrategy.BUFFER)

    var syncState: SyncState = SyncState.NotSynced
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

    val transactionsSubject = PublishSubject.create<Unit>()

    private val balanceSubject = PublishSubject.create<BigDecimal>()
    private val syncStateSubject = PublishSubject.create<SyncState>()
}
