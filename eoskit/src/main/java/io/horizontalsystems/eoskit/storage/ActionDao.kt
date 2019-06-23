package io.horizontalsystems.eoskit.storage

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import io.horizontalsystems.eoskit.models.Action

@Dao
interface ActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(transactions: List<Action>)

    @Query("SELECT * FROM actions WHERE account = 'transfer' ORDER BY sequence DESC")
    fun getAll(): List<Action>

    @RawQuery
    fun getSql(query: SupportSQLiteQuery): List<Action>

    @Query("SELECT * FROM actions ORDER BY sequence DESC LIMIT 1")
    fun getLast(): Action?

    @Query("DELETE FROM actions")
    fun deleteAll()
}
