package it.pagopa.wallet.eventdispatcher.queues

import com.azure.core.util.BinaryData
import com.azure.spring.messaging.checkpoint.Checkpointer
import com.fasterxml.jackson.databind.ObjectMapper
import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.wallet.eventdispatcher.common.queue.QueueEvent
import it.pagopa.wallet.eventdispatcher.configuration.SerializationConfiguration
import it.pagopa.wallet.eventdispatcher.domain.WalletUpdateUsageError
import it.pagopa.wallet.eventdispatcher.domain.WalletUsed
import it.pagopa.wallet.eventdispatcher.service.WalletUsageService
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

class WalletUsageQueueConsumerTest {

    private val checkPointer: Checkpointer = mock()
    private val objectMapper: ObjectMapper =
        SerializationConfiguration().objectMapperBuilder().build()

    private val walletUsageService: WalletUsageService = mock()

    private val consumer =
        WalletUsageQueueConsumer(
            walletUsageService,
            SerializationConfiguration().azureJsonSerializer(objectMapper)
        )

    @BeforeEach
    fun setupTest() {
        reset(checkPointer)
        given { checkPointer.success() }.willAnswer { Mono.empty<Void>() }
        given { checkPointer.failure() }.willAnswer { Mono.empty<Void>() }
    }

    @Test
    fun `should consume a valid event and update checkpoint successfully`() {
        val event =
            "{\"data\":{\"eventId\":\"e7509ed7-dc8d-4089-8501-b01606c6ce43\",\"creationDate\":\"2024-04-09T15:52:12.500329Z\",\"walletId\":\"5063a549-a984-4dd3-8247-892751f7df56\",\"clientId\":\"CHECKOUT\",\"type\":\"WalletUsed\"},\"tracingInfo\":{\"traceparent\":\"mock_traceparent\",\"tracestate\":\"mock_tracestate\",\"baggage\":\"mock_baggage\"}}"
        given { walletUsageService.updateWalletUsage(any(), any(), any()) }
            .willReturn(Mono.just(Unit))
        consumer
            .messageReceiver(BinaryData.fromString(event).toBytes(), checkPointer)
            .test()
            .expectComplete()
            .verify()

        verify(checkPointer, times(1)).success()
    }

    @Test
    fun `should checkpoint event even is event is malformed`() {
        val event = "{\"data\":{ \"wrong_field\": 1}}"
        consumer
            .messageReceiver(BinaryData.fromString(event).toBytes(), checkPointer)
            .test()
            .expectComplete()
            .verify()

        verify(checkPointer, times(1)).success()
    }

    @ParameterizedTest
    @EnumSource(ClientId::class)
    fun `should update wallet usage when receive wallet used event`(clientId: ClientId) {
        val event =
            WalletUsed(
                eventId = UUID.randomUUID().toString(),
                creationDate = OffsetDateTime.now(),
                walletId = UUID.randomUUID().toString(),
                clientId = clientId.name
            )
        given { walletUsageService.updateWalletUsage(any(), any(), any()) }
            .willReturn(Mono.just(Unit))
        consumer
            .messageReceiver(
                BinaryData.fromString(objectMapper.writeValueAsString(QueueEvent(event))).toBytes(),
                checkPointer
            )
            .test()
            .expectComplete()
            .verify()

        verify(checkPointer, times(1)).success()
        verify(walletUsageService, times(1))
            .updateWalletUsage(
                eq(event.walletId),
                eq(ClientId.fromValue(event.clientId)),
                argThat { toInstant() == event.creationDate.toInstant() }
            )
    }

    @ParameterizedTest
    @EnumSource(ClientId::class)
    fun `should checkpoint event wallet usage update fails`(clientId: ClientId) {
        val event =
            WalletUsed(
                eventId = UUID.randomUUID().toString(),
                creationDate = OffsetDateTime.now(),
                walletId = UUID.randomUUID().toString(),
                clientId = clientId.name
            )
        given { walletUsageService.updateWalletUsage(any(), any(), any()) }
            .willReturn(Mono.error(WalletUpdateUsageError(UUID.fromString(event.walletId))))
        consumer
            .messageReceiver(
                BinaryData.fromString(objectMapper.writeValueAsString(QueueEvent(event))).toBytes(),
                checkPointer
            )
            .test()
            .expectComplete()
            .verify()

        verify(checkPointer, times(1)).success()
        verify(walletUsageService, times(1))
            .updateWalletUsage(
                eq(event.walletId),
                eq(ClientId.fromValue(event.clientId)),
                argThat { toInstant() == event.creationDate.toInstant() }
            )
    }

    @Test
    fun `should fail, but checkpoint event when clientId is not recognized`() {
        val event =
            WalletUsed(
                eventId = UUID.randomUUID().toString(),
                creationDate = OffsetDateTime.now(),
                walletId = UUID.randomUUID().toString(),
                clientId = "UNKNOWN_CLIENT"
            )
        given { walletUsageService.updateWalletUsage(any(), any(), any()) }
            .willReturn(Mono.error(WalletUpdateUsageError(UUID.fromString(event.walletId))))
        consumer
            .messageReceiver(
                BinaryData.fromString(objectMapper.writeValueAsString(QueueEvent(event))).toBytes(),
                checkPointer
            )
            .test()
            .expectComplete()
            .verify()

        verify(checkPointer, times(1)).success()
    }
}
