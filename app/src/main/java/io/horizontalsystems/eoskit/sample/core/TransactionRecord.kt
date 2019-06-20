package io.horizontalsystems.eoskit.sample.core

import java.math.BigDecimal

class TransactionAddress(val address: String, val mine: Boolean)
class TransactionRecord(
        val transactionHash: String,
        val transactionIndex: Int,
        val interTransactionIndex: Int,
        val amount: BigDecimal,
        val timestamp: Long,

        var from: TransactionAddress,
        var to: TransactionAddress,

        val blockHeight: Long?)

