package io.horizontalsystems.eoskit.sample

import io.horizontalsystems.eoskit.EosKit
import io.horizontalsystems.eoskit.models.Transaction
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigDecimal

class EosAdapter(private val eosKit: EosKit, tokenName: String, tokenSymbol: String) {

    private val token = eosKit.register(tokenName, tokenSymbol)

    val name: String
        get() = token.symbol

    val coin: String
        get() = token.token

    val syncState: EosKit.SyncState
        get() = token.syncState

    val balance: BigDecimal
        get() = token.balance

    val syncStateFlowable: Flowable<Unit>
        get() = token.syncStateFlowable.map { Unit }

    val balanceFlowable: Flowable<Unit>
        get() = token.balanceFlowable.map { Unit }

    val transactionsFlowable: Flowable<Unit>
        get() = token.transactionsFlowable.map { Unit }

    val irreversibleBlockFlowable: Flowable<Unit>
        get() = eosKit.irreversibleBlockFlowable.map { Unit }

    fun send(to: String, amount: BigDecimal, memo: String): Single<String> {
        return eosKit.send(token, to, amount, memo)
    }

    fun transactions(fromActionSequence: Int? = null, limit: Int? = null): Single<List<Transaction>> {
        return eosKit.transactions(token, fromActionSequence, limit)
    }
}
