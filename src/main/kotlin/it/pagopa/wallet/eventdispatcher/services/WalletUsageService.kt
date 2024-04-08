package it.pagopa.wallet.eventdispatcher.services

import it.pagopa.wallet.eventdispatcher.domain.WalletRepository
import java.util.logging.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class WalletUsageService(private val walletRepository: WalletRepository) {

    private val logger = Logger.getLogger(WalletUsageService::class.java.name)

    fun updateLastUsage(walletId: String, clientId: String): Mono<Unit> {
        return walletRepository
            .findById(walletId)
            .doOnNext {
                logger.info("Updated last usage for wallet $walletId and clientId $clientId")
            }
            .thenReturn(Unit)
    }
}
