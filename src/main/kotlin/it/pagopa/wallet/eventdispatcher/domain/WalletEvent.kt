package it.pagopa.wallet.eventdispatcher.domain

import java.time.OffsetDateTime

sealed interface WalletEvent {
    val eventId: String
    val creationDate: OffsetDateTime
}

data class WalletUsed(
    override val eventId: String,
    override val creationDate: OffsetDateTime,
    val walletId: String,
    val clientId: String
) : WalletEvent
