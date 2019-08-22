package io.horizontalsystems.eoskit.core.exceptions

import java.math.BigInteger

sealed class BackendError : Exception() {
    abstract val detail: String
    abstract val code: BigInteger

    class BalanceOverdrawnError(override val message: String, override val detail: String, override val code: BigInteger) : BackendError()
    class SymbolPrecisionMismatchError(override val message: String, override val detail: String, override val code: BigInteger) : BackendError()
    class TransferToSelfError(override val message: String, override val detail: String, override val code: BigInteger) : BackendError()
    class AccountNotExistError(override val message: String, override val detail: String, override val code: BigInteger) : BackendError()
    class InsufficientRamError(override val message: String, override val detail: String, override val code: BigInteger) : BackendError()
    class MiscellaneousError(override val message: String, override val detail: String, override val code: BigInteger) : BackendError()
}