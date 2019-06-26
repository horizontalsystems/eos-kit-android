package io.horizontalsystems.eoskit.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class IrreversibleBlock(val height: Int, @PrimaryKey val id: String = "last-irreversible-block")
