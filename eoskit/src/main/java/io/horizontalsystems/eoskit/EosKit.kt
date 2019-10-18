package io.horizontalsystems.eoskit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.horizontalsystems.eoskit.core.InvalidPrivateKey
import io.horizontalsystems.eoskit.core.Token
import io.horizontalsystems.eoskit.managers.ActionManager
import io.horizontalsystems.eoskit.managers.BalanceManager
import io.horizontalsystems.eoskit.managers.TransactionManager
import io.horizontalsystems.eoskit.models.Action
import io.horizontalsystems.eoskit.models.Balance
import io.horizontalsystems.eoskit.models.Transaction
import io.horizontalsystems.eoskit.storage.KitDatabase
import io.horizontalsystems.eoskit.storage.Storage
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import one.block.eosiojava.error.utilities.EOSFormatterError
import one.block.eosiojava.implementations.ABIProviderImpl
import one.block.eosiojava.utilities.EOSFormatter
import one.block.eosiojavaabieosserializationprovider.AbiEosSerializationProviderImpl
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl
import one.block.eosiosoftkeysignatureprovider.SoftKeySignatureProviderImpl
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class EosKit(
        val account: String,
        private val balanceManager: BalanceManager,
        private val actionManager: ActionManager,
        private val transactionManager: TransactionManager,
        private val networkType: NetworkType)
    : BalanceManager.Listener, ActionManager.Listener {

    var irreversibleBlockHeight: Int? = actionManager.irreversibleBlockHeight
    val irreversibleBlockFlowable: Flowable<Int>
        get() = irreversibleBlockSubject.toFlowable(BackpressureStrategy.BUFFER)

    private val tokens = mutableListOf<Token>()
    private val irreversibleBlockSubject = PublishSubject.create<Int>()

    fun register(token: String, symbol: String): Token {
        val newBalance = balanceManager.getBalance(symbol)
        val newToken = Token(token, symbol).apply {
            syncState = SyncState.NotSynced
            balance = newBalance?.value ?: BigDecimal(0)
        }

        tokens.add(newToken)
        balanceManager.sync(account, newToken.token)

        return newToken
    }

    fun unregister(token: Token) {
        tokens.removeAll { it == token }
    }

    fun refresh() {
        tokens.forEach { token ->
            if (token.syncState != SyncState.Synced) {
                token.syncState = SyncState.NotSynced
            }

            balanceManager.sync(account, token.token)
        }

        actionManager.sync(account)
    }

    fun stop() {
        balanceManager.stop()
        actionManager.stop()
    }

    @Throws
    fun send(token: Token, to: String, amount: BigDecimal, memo: String): Single<String> {
        return transactionManager
                .send(account, token.token, to, "$amount ${token.symbol}", memo)
                .doOnSuccess {
                    Observable.timer(2, TimeUnit.SECONDS).subscribe {
                        balanceManager.sync(account, token.token)
                    }
                }
    }

    fun transactions(token: Token, fromSequence: Int? = null, limit: Int? = null): Single<List<Transaction>> {
        return actionManager
                .getActions(account, token, fromSequence, limit)
                .map { list -> list.map { Transaction(it) } }
    }

    fun statusInfo(): Map<String, Any> {
        val statusInfo = LinkedHashMap<String, Any>()

        statusInfo["Irreversible Block Height"] = irreversibleBlockHeight ?: "N/A"
        statusInfo["Sync State"] = getKitSyncState()
        statusInfo["RPC Host"] = getRpcHost(networkType)

        return statusInfo
    }

    private fun getKitSyncState(): String {
        val syncState = when {
            tokens.any { it.syncState == SyncState.Syncing } -> SyncState.Syncing
            tokens.any { it.syncState == SyncState.NotSynced } -> SyncState.NotSynced
            else -> SyncState.Synced
        }
        return syncState.name
    }

    // BalanceManager Listener

    override fun onSyncBalance(balance: Balance) {
        tokenBy(balance.token, balance.symbol)?.let { token ->
            token.balance = balance.value
            token.syncState = SyncState.Synced
        }
    }

    override fun onSyncBalanceFail(token: String) {
        tokens.find { it.token == token }?.syncState = SyncState.NotSynced
    }

    // ActionManager Listener

    override fun onSyncActions(actions: List<Action>) {
        actions.groupBy { it.account }.forEach { (token, acts) ->
            acts.map { Transaction(it) }
                    .groupBy { it.symbol }
                    .forEach { (symbol, transactions) ->
                        tokenBy(token, symbol)?.transactionsSubject?.onNext(transactions)
                    }
        }
    }

    override fun onChangeLastIrreversibleBlock(height: Int) {
        irreversibleBlockHeight = height
        irreversibleBlockSubject.onNext(height)
    }

    private fun tokenBy(name: String, symbol: String?): Token? {
        return tokens.find { it.token == name && it.symbol == symbol }
    }

    // SyncState

    enum class SyncState {
        Synced,
        NotSynced,
        Syncing
    }

    enum class NetworkType(chainId: String) {
        MainNet("aca376f206b8fc25a6ed44dbdc66547c36c6c33e3a119ffbeaef943642f0e906"), // EOS
        TestNet("e70aaab8997e1dfce58fbfac80cbbb8fecec7b99cf982a9444273cbc64c41473")  // JUNGLE
    }

    companion object {

        private fun getRpcHost(networkType: NetworkType): String = when (networkType) {
            NetworkType.MainNet -> "https://eos.greymass.com"
            NetworkType.TestNet -> "https://peer1-jungle.eosphere.io"
        }

        fun instance(context: Context, account: String, privateKey: String, networkType: NetworkType = NetworkType.MainNet, walletId: String = "unique-id"): EosKit {
            val database = KitDatabase.create(context, getDatabaseName(networkType, walletId))
            val storage = Storage(database)

            val rpcProvider = EosioJavaRpcProviderImpl(getRpcHost(networkType))
            val serializationProvider = AbiEosSerializationProviderImpl()
            val abiProvider = ABIProviderImpl(rpcProvider, serializationProvider)
            val signatureProvider = SoftKeySignatureProviderImpl().apply {
                importKey(privateKey)
            }

            val balanceManager = BalanceManager(storage, rpcProvider)
            val actionManager = ActionManager(storage, rpcProvider)
            val transactionManager = TransactionManager(rpcProvider, signatureProvider, serializationProvider, abiProvider)

            val eosKit = EosKit(account, balanceManager, actionManager, transactionManager, networkType)

            balanceManager.listener = eosKit
            actionManager.listener = eosKit

            return eosKit
        }

        fun clear(context: Context, networkType: NetworkType, walletId: String) {
            SQLiteDatabase.deleteDatabase(context.getDatabasePath(getDatabaseName(networkType, walletId)))
        }

        fun validatePrivateKey(key: String) {
            try {
                EOSFormatter.convertEOSPrivateKeyToPEMFormat(key)
            } catch (e: EOSFormatterError) {
                throw InvalidPrivateKey()
            }
        }

        private fun getDatabaseName(networkType: NetworkType, walletId: String): String {
            return "Eos-$networkType-$walletId"
        }
    }
}
