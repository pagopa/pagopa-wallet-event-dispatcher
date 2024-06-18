package it.pagopa.wallet.eventdispatcher.api

import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.generated.wallets.model.UpdateWalletUsageRequest
import it.pagopa.generated.wallets.model.WalletStatusPatchRequest
import it.pagopa.wallet.eventdispatcher.exceptions.WalletPatchStatusError
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
            .thenReturn(Unit)
    }

    fun updateWalletStatus(
        walletId: UUID,
        walletStatusPatchRequest: WalletStatusPatchRequest
    ): Mono<Unit> {
        return walletsApiClient
            .patchWalletWithHttpInfo(walletId, walletStatusPatchRequest)
            .doOnNext {
                logger.info(
                    "Wallet status patched successfully! walletId [{}] to status: [{}]",
                    walletId,
                    walletStatusPatchRequest.status
                )
            }
            .onErrorMap { error ->
                val (httpResponseCode, httpResponseBody) =
                    Optional.of(error)
                        .filter { it is WebClientResponseException }
                        .map { it as WebClientResponseException }
                        .map { it.statusCode.value() to it.responseBodyAsString }
                        .orElse(0 to "N/A")
                logger.error(
                    "Error patching wallet status: HTTP status code: [$httpResponseCode], response body: [$httpResponseBody]",
                    error
                )
                WalletPatchStatusError(
                    walletId = walletId,
                    patchWalletStatus = walletStatusPatchRequest.status,
                    cause = error
                )
            }
            .thenReturn(Unit)
    }
}
