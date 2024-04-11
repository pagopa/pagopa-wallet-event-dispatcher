package it.pagopa.wallet.eventdispatcher.service

import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.wallet.eventdispatcher.api.WalletsApi
import it.pagopa.wallet.eventdispatcher.utils.Tracing
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.util.*

class WalletUsageService(
    private val walletsApi: WalletsApi
) {

    private val log = LoggerFactory.getLogger(WalletUsageService::class.java.name)

    private fun updateWalletUsage(
        walletId: String,
        clientId: ClientId,
        usedAt: OffsetDateTime
    ): Mono<Unit> {
        log.info("Updating wallet usage walletId: [{}], client: [{}]", walletId, clientId)
        return walletsApi.updateWalletUsage(
            UUID.fromString(walletId),
            clientId,
            usedAt
        ).filter { it.statusCode.is2xxSuccessful }
            .doOnNext {
                log.info("Wallet last usage updated, walletId: [{}], client: [{}]", walletId, clientId)
            }
            .doOnError {
                log.error("Failed last usage update, walletId: [{}], client: [{}]", walletId, clientId, it)
            }
            .thenReturn(Unit)
            .contextWrite { it.put(Tracing.MDC_WALLET_ID, walletId) }
    }

}