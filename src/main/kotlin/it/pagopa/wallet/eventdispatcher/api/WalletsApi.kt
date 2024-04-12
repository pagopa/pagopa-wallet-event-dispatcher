package it.pagopa.wallet.eventdispatcher.api

import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.generated.wallets.model.UpdateWalletUsageRequest
import java.time.OffsetDateTime
import java.util.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WalletsApi(private val walletsApiClient: it.pagopa.generated.wallets.api.WalletsApi) {

    fun updateWalletUsage(
        walletId: UUID,
        clientId: ClientId,
        usedAt: OffsetDateTime
    ): Mono<ResponseEntity<Unit>> {
        return walletsApiClient
            .updateWalletUsageWithHttpInfo(
                walletId,
                UpdateWalletUsageRequest().clientId(clientId).usageTime(usedAt)
            )
            .map { ResponseEntity(Unit, it.headers, it.statusCode) }
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
    }
}
