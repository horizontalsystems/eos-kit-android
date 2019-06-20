package io.horizontalsystems.eoskit.sample

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.eoskit.EosKit
import io.horizontalsystems.eoskit.EosKit.SyncState
import io.horizontalsystems.eoskit.models.Action
import io.horizontalsystems.eoskit.sample.core.EosAdapter
import io.horizontalsystems.eoskit.sample.core.TokenAdapter
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

class MainViewModel : ViewModel() {

    private val disposables = CompositeDisposable()

    private lateinit var eosKit: EosKit
    private lateinit var eosAdapter: EosAdapter
    private lateinit var tokenAdapter: TokenAdapter

    val balance = MutableLiveData<BigDecimal>()
    val transactions = MutableLiveData<List<Action>>()
    val balanceToken = MutableLiveData<BigDecimal>()
    val syncState = MutableLiveData<SyncState>()
    val sendStatus = SingleLiveEvent<Throwable?>()

    init {
        init()
    }

    //
    // EOS
    //

    fun refresh() {
        eosKit.refresh()
    }

    fun clear() {
        init()
    }

    fun receiveAddress(): String {
        return ""
    }

    fun send(address: String, amount: String, memo: String) {
        eosKit.send(address, amount, memo)
    }

    // Private

    private fun init() {
        eosKit = EosKit(App.instance, "https://peer1-jungle.eosphere.io", "talgattest11")
        eosAdapter = EosAdapter(eosKit)
        tokenAdapter = TokenAdapter(eosKit)

        updateBalance()
        updateState()

        // EOS
        eosAdapter.balanceFlowable
                .subscribe { updateBalance() }
                .let { disposables.add(it) }

        eosAdapter.transactionsFlowable
                .subscribe { updateTransactions() }
                .let { disposables.add(it) }

        eosAdapter.syncStateFlowable
                .subscribe { updateState() }
                .let { disposables.add(it) }

        eosKit.start()
    }

    private fun updateBalance() {
        balance.postValue(eosAdapter.balance)
        balanceToken.postValue(tokenAdapter.balance)
    }

    private fun updateTransactions() {
        transactions.postValue(eosKit.transactions("eosio.token"))
    }

    private fun updateState() {
        syncState.postValue(eosAdapter.syncState)
    }
}
