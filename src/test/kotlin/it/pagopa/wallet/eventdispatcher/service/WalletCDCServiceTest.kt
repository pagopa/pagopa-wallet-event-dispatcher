package it.pagopa.wallet.eventdispatcher.service

import it.pagopa.wallet.eventdispatcher.audit.WalletAddedEvent
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.Logger
import org.springframework.kafka.core.KafkaTemplate
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class WalletCDCServiceTest {

    private val cdcKafkaTemplate: KafkaTemplate<String, Any> = mock()
    private val cdcTopicName: String = "test-topic"
    private val walletCDCService = WalletCDCService(cdcKafkaTemplate, cdcTopicName)
    private val log: Logger = mock()

    @Test
    fun `should successfully send CDC event to Kafka`() {
        val event = WalletAddedEvent(UUID.randomUUID().toString())
        given { cdcKafkaTemplate.send(any(), any(), any()) }.willReturn(mock())

        val result = walletCDCService.sendToKafka(event)

        StepVerifier.create(result).expectNext(Unit).verifyComplete()

        verify(cdcKafkaTemplate).send(cdcTopicName, event.id, event)
    }

    @ParameterizedTest
    @ValueSource(classes = [RuntimeException::class, IllegalArgumentException::class])
    fun `should handle error while sending CDC event to Kafka`(
        exceptionClass: Class<out Throwable>
    ) {
        val event = WalletAddedEvent(UUID.randomUUID().toString())
        given { cdcKafkaTemplate.send(any(), any(), any()) }.willThrow(exceptionClass)

        val result = walletCDCService.sendToKafka(event)

        StepVerifier.create(result).expectErrorMatches { it::class.java == exceptionClass }.verify()

        verify(cdcKafkaTemplate).send(cdcTopicName, event.id, event)
    }

    @Test
    fun `should log success when CDC event is sent to Kafka`() {
        val event = WalletAddedEvent(UUID.randomUUID().toString())
        given { cdcKafkaTemplate.send(any(), any(), any()) }.willReturn(mock())

        walletCDCService.sendToKafka(event).block()

        verify(log).info("Successfully sent CDC event to Kafka: [{}]", event.id)
    }

    @Test
    fun `should log error when failing to send CDC event to Kafka`() {
        val event = WalletAddedEvent(UUID.randomUUID().toString())
        val exception = RuntimeException("Kafka send failed")
        given { cdcKafkaTemplate.send(any(), any(), any()) }.willThrow(exception)

        walletCDCService.sendToKafka(event).onErrorResume { Mono.empty() }.block()

        verify(log).error("Failed to send CDC event to Kafka: [{}]", event.id, exception)
    }
}
