package io.horizontalsystems.eoskit.managers

import io.reactivex.Single
import one.block.eosiojava.interfaces.IABIProvider
import one.block.eosiojava.interfaces.IRPCProvider
import one.block.eosiojava.interfaces.ISerializationProvider
import one.block.eosiojava.interfaces.ISignatureProvider
import one.block.eosiojava.models.rpcProvider.Action
import one.block.eosiojava.models.rpcProvider.Authorization
import one.block.eosiojava.session.TransactionSession
import org.json.JSONObject

class TransactionManager(
        private val account: String,
        private val rpcProvider: IRPCProvider,
        private val signatureProvider: ISignatureProvider,
        private val serializationProvider: ISerializationProvider,
        private val abiProvider: IABIProvider) {

    fun send(token: String, to: String, quantity: String, memo: String): Single<String> {
        return Single.create<String> {
            it.onSuccess(process(token, to, quantity, memo))
        }
    }

    private fun process(token: String, to: String, quantity: String, memo: String): String {

        val session = TransactionSession(serializationProvider, rpcProvider, abiProvider, signatureProvider)
        val processor = session.transactionProcessor

        //  Apply actions data to Action's data
        val reqJson = JSONObject().apply {
            put("from", account)
            put("to", to)
            put("quantity", quantity)
            put("memo", memo)
        }

        val action = Action(token, "transfer", listOf(Authorization(account, "active")), reqJson.toString())

        // Prepare actions with above actions. A actions can be executed with multiple actions.
        processor.prepare(listOf(action))

        //  Sign and broadcast the actions.
        val response = processor.signAndBroadcast()

        return response.transactionId
    }
}
