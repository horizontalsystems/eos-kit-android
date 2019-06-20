package io.horizontalsystems.eoskit.storage

import androidx.sqlite.db.SimpleSQLiteQuery
import io.horizontalsystems.eoskit.core.IStorage
import io.horizontalsystems.eoskit.models.Action
import io.horizontalsystems.eoskit.models.Balance

class Storage(private val database: KitDatabase) : IStorage {

    // Balance

    override fun getBalance(symbol: String): Balance? {
        return database.balance.getBalance(symbol)
    }

    override fun setBalances(balances: List<Balance>) {
        database.balance.insertAll(balances)
    }

    // Actions

    override val lastAction: Action?
        get() = database.actions.getLast()

    override fun setActions(actions: List<Action>) {
        database.actions.insertAll(actions)
    }

    override fun getActions(token: String, fromSequence: Int?, limit: Int?): List<Action> {
        var query = "SELECT * FROM actions WHERE type = 'transfer'"

        if (fromSequence != null) {
            query += " WHERE sequence < $fromSequence"
        }

        query += " ORDER BY sequence DESC"

        if (limit != null) {
            query += " LIMIT $limit"
        }

        println(query)

        return database.actions.getSql(SimpleSQLiteQuery(query))
    }
}
