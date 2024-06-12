package it.pagopa.wallet.eventdispatcher.queues

import com.azure.spring.messaging.checkpoint.Checkpointer
import com.fasterxml.jackson.databind.ObjectMapper
import it.pagopa.wallet.eventdispatcher.configuration.SerializationConfiguration
import java.nio.charset.StandardCharsets
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WalletExpirationQueueConsumerTest {

    private val checkPointer: Checkpointer = mock()
    private val serializationConfiguration = SerializationConfiguration()
    private val objectMapper: ObjectMapper =
        serializationConfiguration.objectMapperBuilder().build()
    private val azureJsonSerializer = serializationConfiguration.azureJsonSerializer(objectMapper)

    private val walletExpirationQueueConsumer =
        WalletExpirationQueueConsumer(azureJsonSerializer = azureJsonSerializer)

    @Test
    fun `Should parse wallet created event successfully`() {
        val queueEvent =
            """
            {
                "data": {
                    "type": "WalletExpired",
                    "eventId": "bcfb7296-c53f-4840-9977-84a597fca1a0",
                    "creationDate": 1718206463.077352000,
                    "walletId": "a21e0037-251d-413b-b121-8899e368df7e"
                },
                "tracingInfo": {
                    "traceparent": "traceparent",
                    "tracestate": "tracestate",
                    "baggage": "baggage"
                }
            }
        """
                .trimIndent()
        val payload = queueEvent.toByteArray(StandardCharsets.UTF_8)
        given(checkPointer.success()).willReturn(Mono.empty())
        StepVerifier.create(
                walletExpirationQueueConsumer.messageReceiver(
                    payload = payload,
                    checkPointer = checkPointer
                )
            )
            .expectNext(Unit)
            .verifyComplete()
        verify(checkPointer, times(1)).success()
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
