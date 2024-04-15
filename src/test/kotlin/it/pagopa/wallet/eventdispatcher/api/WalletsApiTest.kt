package it.pagopa.wallet.eventdispatcher.api

import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.wallet.eventdispatcher.configuration.WebClientConfiguration
import it.pagopa.wallet.eventdispatcher.configuration.properties.WalletsApiConfiguration
import java.time.OffsetDateTime
import java.util.UUID
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.kotlin.test.test

class WalletsApiTest {

    companion object {
        private val mockWebService = MockWebServer()

        @JvmStatic
        @BeforeAll
        fun setup() {
            mockWebService.start(8888)
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            mockWebService.shutdown()
        }
    }

    private val walletsApi =
        WalletsApi(
            WebClientConfiguration()
                .walletsApiClient(
                    WalletsApiConfiguration(
                        uri = "http://localhost:8888",
                        readTimeout = 1000,
                        connectionTimeout = 1000
                    )
                )
        )

    @Test
    fun `should complete successfully when Wallet response is success`() {
        val walletId = UUID.randomUUID()
        mockWebService.dispatch {
            when (it.path) {
                "/wallets/$walletId/usages" -> MockResponse().setResponseCode(204)
                else -> MockResponse().setResponseCode(400)
            }
        }
        walletsApi
            .updateWalletUsage(walletId, ClientId.IO, OffsetDateTime.now())
            .test()
            .expectNext(Unit)
            .verifyComplete()
    }

    @Test
    fun `should fail when Wallet response is negative`() {
        val walletId = UUID.randomUUID()
        mockWebService.dispatch {
            when (it.path) {
                "/wallets/$walletId/usages" -> MockResponse().setResponseCode(400)
                else -> MockResponse().setResponseCode(400)
            }
        }
        walletsApi
            .updateWalletUsage(walletId, ClientId.IO, OffsetDateTime.now())
            .test()
            .expectError(WebClientResponseException::class.java)
            .verify()
    }

    private fun MockWebServer.dispatch(dispatcher: (request: RecordedRequest) -> MockResponse) {
        this.dispatcher =
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse = dispatcher(request)
            }
    }
}
