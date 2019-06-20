package io.horizontalsystems.eoskit.core

import io.horizontalsystems.eoskit.Blockchain
import io.horizontalsystems.eoskit.EosKit
import io.horizontalsystems.eoskit.models.Balance
import io.horizontalsystems.eoskit.models.Transaction
import io.reactivex.Single
import java.math.BigDecimal

interface IStorage {
    fun getBalance(symbol: String): Balance?
    fun setBalances(balances: List<Balance>)
}

interface IBlockchain {
    var listener: Blockchain.Listener?

    val balance: BigDecimal?
    val syncState: EosKit.SyncState

    fun start()
    fun refresh()
    fun stop()

    fun send(): Single<Transaction>
}
