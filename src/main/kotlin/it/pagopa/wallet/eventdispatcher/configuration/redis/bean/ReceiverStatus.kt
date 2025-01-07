package it.pagopa.wallet.eventdispatcher.configuration.redis.bean

/** Data class that contains status information for a specific event receiver */
data class ReceiverStatus(val name: String, val status: Status)
