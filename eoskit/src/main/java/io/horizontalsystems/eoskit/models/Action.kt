package io.horizontalsystems.eoskit.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actions")
class Action(
        @PrimaryKey
        val sequence: Int,
        val type: String,
        val transactionId: String,
        val blockNumber: Int,
        val blockTime: String,
        val token: String,

        val from: String?,
        val to: String?,
        val amount: String?,
        val symbol: String?,
        val memo: String?
)
