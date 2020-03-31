package com.avito.runner.service.worker.device

import com.google.common.net.InetAddresses

sealed class Serial {

    abstract val value: String

    data class Remote(override val value: String) : Serial() {
        override fun toString(): String {
            return value
        }
    }

    data class Local(override val value: String) : Serial() {
        override fun toString(): String {
            return value
        }
    }

    companion object {
        fun from(value: String): Serial {
            return if (isRemote(value)) Remote(
                value
            ) else Local(value)
        }

        @Suppress("UnstableApiUsage")
        private fun isRemote(serial: String): Boolean {
            return serial.contains(':')
                && InetAddresses.isInetAddress(serial.substringBefore(':'))
        }
    }
}
