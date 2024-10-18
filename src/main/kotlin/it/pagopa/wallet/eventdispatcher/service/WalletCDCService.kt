package it.pagopa.wallet.eventdispatcher.service

import it.pagopa.wallet.eventdispatcher.audit.LoggingEvent
import it.pagopa.wallet.eventdispatcher.configuration.properties.RetrySendPolicyConfig
import java.time.Duration
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.retry.Retry

@Component
class WalletCDCService(
    private val cdcKafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
    private val cdcTopicName: String,
    private val retrySendPolicyConfig: RetrySendPolicyConfig
) {

    private val log = LoggerFactory.getLogger(WalletCDCService::class.java.name)

    fun sendToKafka(event: LoggingEvent): Mono<Unit> {
        log.info("Sending CDC event to Kafka: [{}]", event.id)

        return Mono.defer {
                cdcKafkaTemplate
                    .send(cdcTopicName, event.id, event)
                    .doOnSuccess {
                        log.info("Successfully sent CDC event to Kafka: [{}]", event.id)
                    }
                    .doOnError {
                        log.error("Failed to send CDC event to Kafka: [{}]", event.id, it)
                    }
            }
            .retryWhen(
                Retry.fixedDelay(
                        retrySendPolicyConfig.maxAttempts,
                        Duration.ofMillis(retrySendPolicyConfig.intervalInMs)
                    )
                    .doBeforeRetry {
                        log.warn("Retrying to send CDC event to Kafka: [{}]", event.id)
                    }
            )
            .thenReturn(Unit)
    }
}
