package it.pagopa.wallet.eventdispatcher.service

import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.wallet.eventdispatcher.api.WalletsApi
import it.pagopa.wallet.eventdispatcher.domain.WalletUpdateUsageError
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.*
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

class WalletUsageServiceTest {

    private val walletsClient: it.pagopa.generated.wallets.api.WalletsApi = mock()
    private val service = WalletUsageService(WalletsApi(walletsClient))

    @BeforeEach
    fun setup() {
        reset(walletsClient)
    }

    @ParameterizedTest
    @EnumSource(ClientId::class)
    fun `update wallet usage should return successfully if Wallets API return OK`(
        clientId: ClientId
    ) {
        val walletId = UUID.randomUUID().toString()
        val updateTime = OffsetDateTime.now()

        given { walletsClient.updateWalletUsageWithHttpInfo(any(), any()) }
            .willReturn(Mono.just(ResponseEntity.noContent().build()))

        service
            .updateWalletUsage(walletId, clientId, updateTime)
            .test()
            .assertNext {
                verify(walletsClient)
                    .updateWalletUsageWithHttpInfo(eq(UUID.fromString(walletId)), any())
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @EnumSource(ClientId::class)
    fun `update wallet usage throw error if Wallets API goes error`(clientId: ClientId) {
        val walletId = UUID.randomUUID()
        val updateTime = OffsetDateTime.now()

        given { walletsClient.updateWalletUsageWithHttpInfo(any(), any()) }
            .willReturn(Mono.just(ResponseEntity.badRequest().build()))

        service
            .updateWalletUsage(walletId.toString(), clientId, updateTime)
            .test()
            .expectErrorMatches { it is WalletUpdateUsageError && it.walletId == walletId }
            .verify()
    }
}
