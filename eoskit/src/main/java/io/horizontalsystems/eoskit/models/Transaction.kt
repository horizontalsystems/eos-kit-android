package io.horizontalsystems.eoskit.models

class Transaction(action: Action) {
    val id: String = action.transactionId
    val blockNumber: Int = action.blockNumber
    val actionSequence: Int = action.sequence
    val date: Long = action.blockTime

    val from: String? = action.from
    val to: String? = action.to
    val amount: String? = action.amount
    val symbol: String? = action.symbol
    val memo: String? = action.memo
}
