package it.pagopa.wallet.eventdispatcher.service

import com.azure.messaging.eventhubs.EventData
import com.azure.messaging.eventhubs.EventDataBatch
import com.azure.messaging.eventhubs.EventHubProducerClient
import it.pagopa.wallet.eventdispatcher.audit.LoggingEvent
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WalletCDCService(private val cdcEventHubClient: EventHubProducerClient) {

    private val log = LoggerFactory.getLogger(WalletCDCService::class.java.name)

    fun sendBsonToEventHub(event: LoggingEvent): Mono<Unit> {
        log.info("Sending cdc event to Event Hub: [{}]", event.id)

        return Mono.fromCallable {
                val eventData = EventData(event.toString())
                val eventDataBatch: EventDataBatch = cdcEventHubClient.createBatch()
                eventDataBatch.tryAdd(eventData)
                cdcEventHubClient.send(eventDataBatch)
            }
            .doOnSuccess { log.info("Successfully sent cdc event to Event Hub: [{}]", event.id) }
            .doOnError { log.error("Failed to send cdc event to Event Hub: [{}]", event.id, it) }
            .thenReturn(Unit)
    }
}
