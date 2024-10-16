package it.pagopa.wallet.eventdispatcher.service

import it.pagopa.wallet.eventdispatcher.audit.LoggingEvent
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WalletCDCService(
    private val cdcKafkaTemplate: KafkaTemplate<String, Any>,
    private val cdcTopicName: String
) {

    private val log = LoggerFactory.getLogger(WalletCDCService::class.java.name)

    fun sendToKafka(event: LoggingEvent): Mono<Unit> {
        log.info("Sending CDC event to Kafka: [{}]", event.id)

        return Mono.fromCallable { cdcKafkaTemplate.send(cdcTopicName, event.id, event) }
            .doOnSuccess { log.info("Successfully sent CDC event to Kafka: [{}]", event.id) }
            .doOnError { log.error("Failed to send CDC event to Kafka: [{}]", event.id, it) }
            .thenReturn(Unit)
    }
}
