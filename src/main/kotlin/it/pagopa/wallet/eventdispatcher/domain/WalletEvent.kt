package it.pagopa.wallet.eventdispatcher.domain

import java.time.Instant

sealed interface WalletEvent {
    val id: String
    val createdAt: Instant
}

data class WalletUsed(
    override val id: String,
    override val createdAt: Instant,
    val walletId: String,
    val clientId: String
) : WalletEvent
