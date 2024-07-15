package it.pagopa.wallet.eventdispatcher.queues

import com.azure.spring.messaging.checkpoint.Checkpointer
import com.fasterxml.jackson.databind.ObjectMapper
import it.pagopa.generated.wallets.model.WalletStatusErrorPatchRequest
import it.pagopa.generated.wallets.model.WalletStatusErrorPatchRequestDetails
import it.pagopa.wallet.eventdispatcher.api.WalletsApi
import it.pagopa.wallet.eventdispatcher.common.queue.QueueEvent
import it.pagopa.wallet.eventdispatcher.configuration.SerializationConfiguration
import it.pagopa.wallet.eventdispatcher.domain.WalletCreatedEvent
import it.pagopa.wallet.eventdispatcher.utils.TracingMock
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.*
import kotlinx.coroutines.reactor.mono
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WalletExpirationQueueConsumerTest {

    private val checkPointer: Checkpointer = mock()
    private val serializationConfiguration = SerializationConfiguration()
    private val objectMapper: ObjectMapper =
        serializationConfiguration.objectMapperBuilder().build()
    private val azureJsonSerializer = serializationConfiguration.azureJsonSerializer(objectMapper)
    private val walletsApi: WalletsApi = mock()

    private val walletExpirationQueueConsumer =
        WalletExpirationQueueConsumer(
            azureJsonSerializer = azureJsonSerializer,
            walletsApi = walletsApi,
            tracing = TracingMock.mock()
        )

    @Test
    fun `Should process wallet create event successfully`() {
        val walletId = UUID.randomUUID()
        val creationDate = "2024-06-14T15:04:56.908428Z"
        val walletCreationDate = OffsetDateTime.parse(creationDate)
        val walletCreatedEvent =
            QueueEvent(
                WalletCreatedEvent(
                    walletId = walletId.toString(),
                    eventId = UUID.randomUUID().toString(),
                    creationDate = walletCreationDate
                )
            )
        val baos = ByteArrayOutputStream()
        azureJsonSerializer.createInstance().serialize(baos, walletCreatedEvent)
        val payload = baos.toByteArray()
        given(checkPointer.success()).willReturn(Mono.empty())
        given(walletsApi.updateWalletStatus(any(), any())).willReturn(mono {})
        val expectedPatchRequest =
            WalletStatusErrorPatchRequest()
                .status("ERROR")
                .details(
                    WalletStatusErrorPatchRequestDetails()
                        .reason("Wallet expired. Creation date: $creationDate")
                )
        StepVerifier.create(
                walletExpirationQueueConsumer.messageReceiver(
                    payload = payload,
                    checkPointer = checkPointer
                )
            )
            .expectNext(Unit)
            .verifyComplete()
        verify(checkPointer, times(1)).success()
        verify(walletsApi, times(1))
            .updateWalletStatus(
                walletId = walletId,
                walletStatusPatchRequest = expectedPatchRequest
            )
    }

    @Test
    fun `Should propagate error patching wallet status to ERROR`() {
        val walletId = UUID.randomUUID()
        val creationDate = "2024-06-14T15:04:56.908428Z"
        val walletCreationDate = OffsetDateTime.parse(creationDate)
        val walletCreatedEvent =
            QueueEvent(
                WalletCreatedEvent(
                    walletId = walletId.toString(),
                    eventId = UUID.randomUUID().toString(),
                    creationDate = walletCreationDate
                )
            )
        val baos = ByteArrayOutputStream()
        azureJsonSerializer.createInstance().serialize(baos, walletCreatedEvent)
        val payload = baos.toByteArray()
        given(checkPointer.success()).willReturn(Mono.empty())
        given(walletsApi.updateWalletStatus(any(), any()))
            .willReturn(Mono.error(RuntimeException("Error patching wallet")))
        val expectedPatchRequest =
            WalletStatusErrorPatchRequest()
                .status("ERROR")
                .details(
                    WalletStatusErrorPatchRequestDetails()
                        .reason("Wallet expired. Creation date: $creationDate")
                )
        StepVerifier.create(
                walletExpirationQueueConsumer.messageReceiver(
                    payload = payload,
                    checkPointer = checkPointer
                )
            )
            .expectErrorMatches {
                assertEquals("Error patching wallet", it.message)
                true
            }
            .verify()
        verify(checkPointer, times(1)).success()
        verify(walletsApi, times(1))
            .updateWalletStatus(
                walletId = walletId,
                walletStatusPatchRequest = expectedPatchRequest
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings =
            [
                "{}",
                """
         {
                "data": {
                    "type": "WalletExpired",
                    "eventId": "bcfb7296-c53f-4840-9977-84a597fca1a0",
                    "creationDate": 1718206463.077352000
                },
                "tracingInfo": {
                    "traceparent": "traceparent",
                    "tracestate": "tracestate",
                    "baggage": "baggage"
                }
            }
    """
            ]
    )
    fun `Should perform success checkpoint for invalid event`(queueEvent: String) {
        val payload = queueEvent.toByteArray(StandardCharsets.UTF_8)
        given(checkPointer.success()).willReturn(Mono.empty())
        StepVerifier.create(
                walletExpirationQueueConsumer.messageReceiver(
                    payload = payload,
                    checkPointer = checkPointer
                )
            )
            .expectError()
            .verify()
        verify(checkPointer, times(1)).success()
    }
}
