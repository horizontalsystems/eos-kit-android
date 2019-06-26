package io.horizontalsystems.eoskit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.eoskit.models.IrreversibleBlock

@Dao
interface IrreversibleBlocksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(irreversibleBlock: IrreversibleBlock)

    @Query("SELECT * FROM IrreversibleBlock LIMIT 1")
    fun getLast(): IrreversibleBlock?

    @Query("DELETE FROM IrreversibleBlock")
    fun deleteAll()
}
