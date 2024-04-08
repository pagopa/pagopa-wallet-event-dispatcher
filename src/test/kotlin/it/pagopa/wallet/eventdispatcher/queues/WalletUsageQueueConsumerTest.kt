package it.pagopa.wallet.eventdispatcher.queues

import com.azure.core.util.BinaryData
import com.azure.spring.messaging.checkpoint.Checkpointer
import com.fasterxml.jackson.databind.ObjectMapper
import it.pagopa.wallet.eventdispatcher.common.queue.QueueEvent
import it.pagopa.wallet.eventdispatcher.common.queue.TracingInfo
import it.pagopa.wallet.eventdispatcher.configuration.SerializationConfiguration
import it.pagopa.wallet.eventdispatcher.domain.WalletUsed
import it.pagopa.wallet.eventdispatcher.services.WalletUsageService
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.util.UUID

class WalletUsageQueueConsumerTest {

    private val walletUsageService: WalletUsageService = mock()
    private val checkPointer: Checkpointer = mock()
    private val objectMapper: ObjectMapper =
        SerializationConfiguration().objectMapperBuilder().build()

    private val consumer =
        WalletUsageQueueConsumer(
            SerializationConfiguration().azureJsonSerializer(objectMapper),
            walletUsageService
        )

    @BeforeEach
    fun setupTest() {
        reset(checkPointer)
        given { checkPointer.success() }.willAnswer { Mono.empty<Void>() }
        given { checkPointer.failure() }.willAnswer { Mono.empty<Void>() }
    }

    @Test
    fun `should consume a valid event and update checkpoint successfully`() {
        val event = QueueEvent(
            WalletUsed("event_id", Instant.now(), "wallet_id", "client_id"),
            tracingInfo = randomTracingInfo()
        )
        consumer
            .messageReceiver(
                BinaryData.fromString(objectMapper.writeValueAsString(event)).toBytes(),
                checkPointer
            )
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

    private fun randomTracingInfo() = TracingInfo(
        UUID.randomUUID().toString(),
    )
}
