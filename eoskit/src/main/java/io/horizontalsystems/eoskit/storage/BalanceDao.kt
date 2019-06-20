package io.horizontalsystems.eoskit.storage

import androidx.room.*
import io.horizontalsystems.eoskit.models.Balance

@Dao
interface BalanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(balances: List<Balance>)

    @Query("SELECT * FROM Balance WHERE symbol = :symbol LIMIT 1")
    fun getBalance(symbol: String): Balance?

    @Query("DELETE FROM Balance")
    fun deleteAll()
}
