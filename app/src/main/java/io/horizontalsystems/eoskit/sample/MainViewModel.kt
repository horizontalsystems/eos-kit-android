package io.horizontalsystems.eoskit.sample

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.eoskit.EosKit
import io.horizontalsystems.eoskit.models.Transaction
import io.reactivex.disposables.CompositeDisposable

class MainViewModel : ViewModel() {

    val adapters = mutableListOf<EosAdapter>()

    val syncState = MutableLiveData<EosKit.SyncState>()
    val balance = MutableLiveData<String>()
    val transactions = MutableLiveData<Map<String, List<Transaction>>>()
    val lastIrreversibleBlock = MutableLiveData<Int>()

    private val disposables = CompositeDisposable()

    private lateinit var eosKit: EosKit

    init {
        init()
    }

    fun refresh() {
        eosKit.refresh()
    }

    fun clear() {
        init()
    }

    fun updateActions(adapter: EosAdapter) {
        adapter.transactions()
                .subscribe { list -> transactions.postValue(mapOf(adapter.name to list)) }
                .let { disposables.add(it) }
    }

    // Private

    private fun init() {
        eosKit = EosKit.instance(App.instance, "talgattest11", "5JW3RbdpXrVTHwJzNMCpRsaKk5YEqCKykmZPxqT7MrXXsDhp2PY", EosKit.NetworkType.TestNet)
        adapters.add(EosAdapter(eosKit, "eosio.token", "EOS"))
        adapters.add(EosAdapter(eosKit, "eosio.token", "JUNGLE"))

        adapters.forEach { adapter ->

            updateBalance(adapter)
            updateActions(adapter)

            adapter.balanceFlowable.subscribe {
                balance.postValue("${adapter.balance} ${adapter.name}")
            }

            adapter.syncStateFlowable.subscribe {
                syncState.postValue(adapter.syncState)
            }

            adapter.transactionsFlowable.subscribe {
                updateActions(adapter)
            }

            adapter.irreversibleBlockFlowable.subscribe {
                lastIrreversibleBlock.postValue(eosKit.irreversibleBlockHeight)
            }
        }

        eosKit.refresh()
    }

    private fun updateBalance(adapter: EosAdapter) {
        balance.postValue("${adapter.balance} ${adapter.name}")
    }
}
