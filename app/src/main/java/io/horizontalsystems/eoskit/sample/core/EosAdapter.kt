package io.horizontalsystems.eoskit.sample.core

import io.horizontalsystems.eoskit.EosKit
import io.reactivex.Flowable
import java.math.BigDecimal

class EosAdapter(private val eosKit: EosKit) : IAdapter {

    override val name: String
        get() = "EOSIO"

    override val coin: String
        get() = "EOS"

    override val syncState: EosKit.SyncState
        get() = eosKit.syncState

    override val balance: BigDecimal
        get() = eosKit.getBalance(coin)?.value ?: BigDecimal.ZERO

    override val syncStateFlowable: Flowable<Unit>
        get() = eosKit.syncStateFlowable.map { Unit }

    override val balanceFlowable: Flowable<Unit>
        get() = eosKit.balanceFlowable.map { Unit }

}

class TokenAdapter(private val eosKit: EosKit) : IAdapter {

    override val name: String
        get() = "Jungle"

    override val coin: String
        get() = "JUNGLE"

    override val syncState: EosKit.SyncState
        get() = eosKit.syncState

    override val balance: BigDecimal
        get() = eosKit.getBalance(coin)?.value ?: BigDecimal.ZERO

    override val syncStateFlowable: Flowable<Unit>
        get() = eosKit.syncStateFlowable.map { Unit }

    override val balanceFlowable: Flowable<Unit>
        get() = eosKit.balanceFlowable.map { Unit }

}

