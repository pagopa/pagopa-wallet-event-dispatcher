package it.pagopa.wallet.eventdispatcher.service

import it.pagopa.wallet.eventdispatcher.audit.WalletAddedEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import reactor.kotlin.test.test

@ExtendWith(MockitoExtension::class)
class WalletCDCServiceTest {

    private val cdcKafkaTemplate: KafkaTemplate<String, Any> = mock()
    private val cdcTopicName: String = "test-topic"
    private val walletCDCService = WalletCDCService(cdcKafkaTemplate, cdcTopicName)

    @Test
    fun `should successfully send CDC event to Kafka`() {
        val event = WalletAddedEvent(UUID.randomUUID().toString())
        val sendResult = mock<SendResult<String, Any>>()
        val future = CompletableFuture.completedFuture(sendResult)
        given(cdcKafkaTemplate.send(anyString(), anyString(), any())).willReturn(future)

        walletCDCService.sendToKafka(event).test().assertNext { it is Unit }.verifyComplete()
    }

    @Test
    fun `should log error when failing to send CDC event to Kafka`() {
        val event = WalletAddedEvent(UUID.randomUUID().toString())
        val exception = RuntimeException("Kafka send failed")
        given(cdcKafkaTemplate.send(anyString(), anyString(), any())).willThrow(exception)

        walletCDCService
            .sendToKafka(event)
            .test()
            .expectErrorMatches { it is RuntimeException }
            .verify()
    }
}
