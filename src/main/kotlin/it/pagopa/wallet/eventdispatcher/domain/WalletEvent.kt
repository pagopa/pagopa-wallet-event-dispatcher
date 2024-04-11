package it.pagopa.wallet.eventdispatcher.domain

import java.time.ZonedDateTime

sealed interface WalletEvent {
    val eventId: String
    val creationDate: ZonedDateTime
}

data class WalletUsed(
    override val eventId: String,
    override val creationDate: ZonedDateTime,
    val walletId: String,
    val clientId: String
) : WalletEvent
