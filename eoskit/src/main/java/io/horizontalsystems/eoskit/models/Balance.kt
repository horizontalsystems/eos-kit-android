package io.horizontalsystems.eoskit.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity
class Balance(@PrimaryKey val symbol: String, val value: BigDecimal, val token: String)
