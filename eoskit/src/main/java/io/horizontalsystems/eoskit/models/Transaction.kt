package io.horizontalsystems.eoskit.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Transaction(@PrimaryKey val key: String)
