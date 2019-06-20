package io.horizontalsystems.eoskit.sample.core

import io.horizontalsystems.eoskit.EosKit
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigDecimal

interface IAdapter {

    val name: String
    val coin: String

    val syncState: EosKit.SyncState
    val balance: BigDecimal

    val syncStateFlowable: Flowable<Unit>
    val balanceFlowable: Flowable<Unit>
    val transactionsFlowable: Flowable<Unit>
}
