package io.horizontalsystems.eoskit.core

import io.horizontalsystems.eoskit.models.Action
import io.horizontalsystems.eoskit.models.Balance
import io.horizontalsystems.eoskit.models.IrreversibleBlock

interface IStorage {
    fun setBalances(balances: List<Balance>)
    fun getBalance(symbol: String): Balance?

    val lastAction: Action?
    fun setActions(actions: List<Action>)
    fun getActions(token: String, symbol: String, receiver: String, fromSequence: Int?, limit: Int?): List<Action>

    val lastIrreversibleBlock: IrreversibleBlock?
    fun setIrreversibleBlock(height: Int)
}
