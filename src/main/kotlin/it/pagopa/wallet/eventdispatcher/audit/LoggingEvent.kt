package it.pagopa.wallet.eventdispatcher.audit

import java.time.Instant
import java.util.*

sealed class LoggingEvent(val id: String, val timestamp: String) {
    constructor() : this(UUID.randomUUID().toString(), Instant.now().toString())
}

data class WalletAddedEvent(val walletId: String) : LoggingEvent()

data class WalletMigratedAddedEvent(val walletId: String) : LoggingEvent()

data class WalletDeletedEvent(val walletId: String) : LoggingEvent()

data class WalletDetailsAddedEvent(val walletId: String) : LoggingEvent()

data class ApplicationCreatedEvent(val serviceId: String) : LoggingEvent()
