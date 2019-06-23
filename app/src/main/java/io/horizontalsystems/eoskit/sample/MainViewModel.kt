package io.horizontalsystems.eoskit.sample

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.eoskit.EosKit
import io.horizontalsystems.eoskit.models.Transaction

class MainViewModel : ViewModel() {

    val adapters = mutableListOf<EosAdapter>()

    val syncState = MutableLiveData<EosKit.SyncState>()
    val balance = MutableLiveData<String>()
    val transactions = MutableLiveData<List<Transaction>>()

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

    // Private

    private fun init() {
        eosKit = EosKit.create(App.instance, "talgattest11", "5JW3RbdpXrVTHwJzNMCpRsaKk5YEqCKykmZPxqT7MrXXsDhp2PY", EosKit.NetworkType.TestNet)
        adapters.add(EosAdapter(eosKit, "eosio.token", "EOS"))

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
                transactions.postValue(adapter.transactions())
            }
        }

        eosKit.refresh()
    }

    private fun updateBalance(adapter: EosAdapter) {
        balance.postValue("${adapter.balance} ${adapter.name}")
    }

    private fun updateActions(adapter: EosAdapter) {
        transactions.postValue(adapter.transactions())
    }
}
