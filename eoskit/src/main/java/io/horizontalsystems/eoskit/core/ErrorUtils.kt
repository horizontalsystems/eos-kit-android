package io.horizontalsystems.eoskit.core

import io.horizontalsystems.eoskit.core.exceptions.BackendError
import one.block.eosiojava.error.EosioError
import one.block.eosiojava.models.rpcProvider.response.RPCResponseError
import one.block.eosiojavarpcprovider.error.EosioJavaRpcProviderCallError
import java.math.BigInteger

object ErrorUtils {

    /**
     * Recursively look for a specific error inside causes loop of an EosioError
     *
     * @param errorClass - the error class to find
     * @param error      - the error object to search
     * @param <T>        - the generic class which extends from EosioError
     * @return the error which class is specified by input. Return null if could not find the specific class.
    </T> */
    fun <T : Exception> getErrorObject(errorClass: Class<*>, error: Exception): T? {
        if (error.javaClass == errorClass) {
            return error as T
        }

        return if (error.cause == null) {
            null
        } else getErrorObject(errorClass, error.cause as Exception)

        // Recursively look deeper
    }

    /**
     * Get backend error class [RPCResponseError] if an backend error is available
     *
     * @param error the error class to get the backend error
     * @return [RPCResponseError] object. Return null if input error does not contain any backend error.
     */
    fun getBackendError(error: EosioError): RPCResponseError? {
        val rpcError =
            getErrorObject<EosioJavaRpcProviderCallError>(EosioJavaRpcProviderCallError::class.java, error)
        return rpcError?.rpcResponseError
    }

    /**
     * Format and return a back end error from a [RPCResponseError] object
     *
     * @param error the RPC backend error
     * @return Formatted backend error from input
     */
    fun getBackendErrorFromResponse(error: RPCResponseError): BackendError {
        val detail = StringBuilder()
        if (error.error.details.isNotEmpty()) {
            for (errorDetail in error.error.details) {
                detail.append(errorDetail.message).append(" - ")
            }
        }

        return getBackendError(error.message, detail.toString(), error.error.code)
    }

    private fun getBackendError(message: String, detail: String, code: BigInteger): BackendError {
        val error = when (code) {
            3050003.toBigInteger() -> when {
                detail.contains("account does not exist") -> BackendError.AccountNotExistError(message, detail, code)
                detail.contains("overdrawn") -> BackendError.BalanceOverdrawnError(message, detail, code)
                detail.contains("symbol precision mismatch") -> BackendError.SymbolPrecisionMismatchError(message, detail, code)
                detail.contains("cannot transfer to self") -> BackendError.TransferToSelfError(message, detail, code)
                else -> null
            }
            3050001.toBigInteger() -> when {
                detail.contains("insufficient ram") -> BackendError.InsufficientRamError(message, detail, code)
                else -> null
            }
            else -> null
        }
        return error ?: BackendError.MiscellaneousError(message, detail, code)
    }
}
