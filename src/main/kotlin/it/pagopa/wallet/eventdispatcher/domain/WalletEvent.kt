package it.pagopa.wallet.eventdispatcher.domain

import java.time.Instant

sealed interface WalletEvent {
    val eventId: String
    val creationDate: Instant
}

data class WalletUsed(
    override val eventId: String,
    override val creationDate: Instant,
    val walletId: String,
    val clientId: String
) : WalletEvent
