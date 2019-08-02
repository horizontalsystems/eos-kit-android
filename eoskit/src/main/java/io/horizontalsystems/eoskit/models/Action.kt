package io.horizontalsystems.eoskit.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actions")
class Action(
        @PrimaryKey
        val sequence: Int,
        val name: String,
        val account: String,
        val transactionId: String,
        val blockNumber: Int,
        val blockTime: Long,

        val receiver: String,
        val from: String?,
        val to: String?,
        val amount: String?,
        val symbol: String?,
        val memo: String?
)
