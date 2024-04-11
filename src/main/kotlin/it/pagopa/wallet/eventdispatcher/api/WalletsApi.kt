package it.pagopa.wallet.eventdispatcher.api

import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.generated.wallets.model.UpdateWalletUsageRequest
import java.time.Instant
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
        usedAt: Instant
    ): Mono<ResponseEntity<Unit>> {
        return walletsApiClient
            .updateWalletUsageWithResponseSpec(
                walletId,
                UpdateWalletUsageRequest().clientId(clientId).usageTime(OffsetDateTime.from(usedAt))
            )
            .toBodilessEntity()
            .map { ResponseEntity(Unit, it.headers, it.statusCode) }
    }
}
