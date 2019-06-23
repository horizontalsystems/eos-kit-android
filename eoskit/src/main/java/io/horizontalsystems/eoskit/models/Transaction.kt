package io.horizontalsystems.eoskit.models

class Transaction(action: Action) {
    val id: String = action.transactionId
    val actionSequence: Int = action.sequence
    val date: String = action.blockTime

    val from: String? = action.from
    val to: String? = action.to
    val amount: String? = action.amount
    val symbol: String? = action.symbol
    val memo: String? = action.memo
}
