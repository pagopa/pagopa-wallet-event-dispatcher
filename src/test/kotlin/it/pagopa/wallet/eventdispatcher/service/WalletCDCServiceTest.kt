package it.pagopa.wallet.eventdispatcher.service

import it.pagopa.wallet.eventdispatcher.audit.WalletAddedEvent
import it.pagopa.wallet.eventdispatcher.configuration.properties.RetrySendPolicyConfig
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderResult
import reactor.kotlin.test.test

@ExtendWith(MockitoExtension::class)
class WalletCDCServiceTest {

    private val retrySendPolicyConfig: RetrySendPolicyConfig = RetrySendPolicyConfig(1, 100)
    private val cdcKafkaTemplate: ReactiveKafkaProducerTemplate<String, Any> = mock()
    private val cdcTopicName: String = "test-topic"
    private val walletCDCService =
        WalletCDCService(cdcKafkaTemplate, cdcTopicName, retrySendPolicyConfig)

    @Test
    fun `should successfully send CDC event to Kafka`() {
        val event = WalletAddedEvent(UUID.randomUUID().toString())
        val sendResult = mock<SenderResult<Void>>()
        given(cdcKafkaTemplate.send(anyString(), anyString(), any()))
            .willReturn(Mono.just(sendResult))

        walletCDCService.sendToKafka(event).test().assertNext { it is Unit }.verifyComplete()
    }

    @Test
    fun `should log error when failing to send CDC event to Kafka`() {
        val event = WalletAddedEvent(UUID.randomUUID().toString())
        given(cdcKafkaTemplate.send(anyString(), anyString(), any()))
            .willAnswer { Mono.error<RuntimeException>(RuntimeException("First attempt failed")) }
            .willAnswer { Mono.error<RuntimeException>(RuntimeException("Second attempt failed")) }
        walletCDCService
            .sendToKafka(event)
            .test()
            .expectErrorMatches { it is RuntimeException }
            .verify()
    }

    @Test
    fun `should dispatch event from on second retry`() {
        val event = WalletAddedEvent(UUID.randomUUID().toString())
        val sendResult = mock<SenderResult<Void>>()
        given(cdcKafkaTemplate.send(anyString(), anyString(), any()))
            .willAnswer { Mono.error<RuntimeException>(RuntimeException("First attempt failed")) }
            .willReturn(Mono.just(sendResult))

        walletCDCService.sendToKafka(event).test().assertNext { it is Unit }.verifyComplete()
    }
}
