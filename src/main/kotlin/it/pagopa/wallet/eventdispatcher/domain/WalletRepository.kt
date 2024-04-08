package it.pagopa.wallet.eventdispatcher.domain

import it.pagopa.wallet.eventdispatcher.repositories.WalletDocument
import reactor.core.publisher.Mono

interface WalletRepository {
    fun findById(walletId: String): Mono<WalletDocument>
}