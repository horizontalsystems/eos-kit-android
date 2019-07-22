package io.horizontalsystems.eoskit.core

import one.block.eosiojava.error.utilities.EOSFormatterError
import one.block.eosiojava.utilities.EOSFormatter

object EosUtils {
    fun validatePrivateKey(key: String) {
        try {
            EOSFormatter.convertEOSPrivateKeyToPEMFormat(key)
        } catch (e: EOSFormatterError) {
            throw PrivateKeyFormatError()
        }
    }
}

class PrivateKeyFormatError : Exception()
