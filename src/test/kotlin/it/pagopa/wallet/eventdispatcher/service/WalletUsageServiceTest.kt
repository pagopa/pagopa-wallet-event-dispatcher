package it.pagopa.wallet.eventdispatcher.service

import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.wallet.eventdispatcher.api.WalletsApi
import it.pagopa.wallet.eventdispatcher.domain.WalletUpdateUsageError
import java.time.Instant
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.*
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

class WalletUsageServiceTest {

    private val walletsClient: WalletsApi = mock()
    private val service = WalletUsageService(walletsClient)

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
        val updateTime = Instant.now()

        given { walletsClient.updateWalletUsage(any(), any(), any()) }
            .willReturn(Mono.just(ResponseEntity.noContent().build()))

        service
            .updateWalletUsage(walletId, clientId, updateTime)
            .test()
            .assertNext {
                verify(walletsClient)
                    .updateWalletUsage(eq(UUID.fromString(walletId)), eq(clientId), eq(updateTime))
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @EnumSource(ClientId::class)
    fun `update wallet usage throw error if Wallets API goes error`(clientId: ClientId) {
        val walletId = UUID.randomUUID()
        val updateTime = Instant.now()

        given { walletsClient.updateWalletUsage(any(), any(), any()) }
            .willReturn(Mono.just(ResponseEntity.badRequest().build()))

        service
            .updateWalletUsage(walletId.toString(), clientId, updateTime)
            .test()
            .expectErrorMatches { it is WalletUpdateUsageError && it.walletId == walletId }
            .verify()
    }
}
