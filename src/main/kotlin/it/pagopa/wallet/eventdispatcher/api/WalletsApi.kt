package it.pagopa.wallet.eventdispatcher.api

import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.generated.wallets.model.UpdateWalletUsageRequest
import java.time.OffsetDateTime
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class WalletsApi(private val walletsApiClient: it.pagopa.generated.wallets.api.WalletsApi) {

    private val logger = LoggerFactory.getLogger(WalletsApi::class.java.name)

    fun updateWalletUsage(walletId: UUID, clientId: ClientId, usedAt: OffsetDateTime): Mono<Unit> {
        return walletsApiClient
            .updateWalletUsageWithHttpInfo(
                walletId,
                UpdateWalletUsageRequest().clientId(clientId).usageTime(usedAt)
            )
            .doOnError {
                if (it is WebClientResponseException) {
                    logger.error(
                        "Error updating last usage data. Received HTTP error code: [${it.statusCode}], response body: [${it.responseBodyAsString}]",
                        it
                    )
                } else {
                    logger.error("Error updating last usage data.", it)
                }
            }
            .map {}
    }
}
