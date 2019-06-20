package io.horizontalsystems.eoskit.storage

import io.horizontalsystems.eoskit.core.IStorage
import io.horizontalsystems.eoskit.models.Balance

class Storage(private val database: KitDatabase) : IStorage {
    override fun getBalance(symbol: String): Balance? {
        return database.balance.getBalance(symbol)
    }

    override fun setBalances(balances: List<Balance>) {
        database.balance.insertAll(balances)
    }
}
