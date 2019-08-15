package io.horizontalsystems.eoskit.core

import java.math.BigInteger

class InvalidPrivateKey : Exception()

class BackendError(override val message: String, val detail: String, val code: BigInteger): Exception(message)
