package io.horizontalsystems.eoskit.storage

import android.content.Context
import androidx.room.*
import io.horizontalsystems.eoskit.models.Action
import io.horizontalsystems.eoskit.models.Balance
import io.horizontalsystems.eoskit.models.IrreversibleBlock
import java.math.BigDecimal

@Database(version = 1, exportSchema = false, entities = [
    Balance::class,
    Action::class,
    IrreversibleBlock::class
])

@TypeConverters(Converters::class)
abstract class KitDatabase : RoomDatabase() {
    abstract val balance: BalanceDao
    abstract val actions: ActionDao
    abstract val irreversibleBlock: IrreversibleBlocksDao

    companion object {
        fun create(context: Context, dbName: String): KitDatabase {
            return Room.databaseBuilder(context, KitDatabase::class.java, dbName)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
        }
    }
}

class Converters {
    @TypeConverter
    fun bigDecimalFromString(string: String): BigDecimal {
        return BigDecimal(string)
    }

    @TypeConverter
    fun bigDecimalToString(bigDecimal: BigDecimal): String {
        return bigDecimal.toString()
    }
}
