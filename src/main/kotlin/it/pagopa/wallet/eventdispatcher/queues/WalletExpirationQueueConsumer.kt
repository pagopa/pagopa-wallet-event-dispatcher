package it.pagopa.wallet.eventdispatcher.queues

import com.azure.core.util.BinaryData
import com.azure.core.util.serializer.JsonSerializerProvider
import com.azure.core.util.serializer.TypeReference
import com.azure.spring.messaging.AzureHeaders
import com.azure.spring.messaging.checkpoint.Checkpointer
import it.pagopa.wallet.eventdispatcher.common.queue.QueueEvent
import it.pagopa.wallet.eventdispatcher.configuration.QueueConsumerConfiguration
import it.pagopa.wallet.eventdispatcher.domain.WalletCreatedEvent
import it.pagopa.wallet.eventdispatcher.domain.WalletEvent
import org.slf4j.LoggerFactory
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class WalletExpirationQueueConsumer(azureJsonSerializer: JsonSerializerProvider) {

    private val azureSerializer = azureJsonSerializer.createInstance()

    companion object {
        const val INPUT_CHANNEL = QueueConsumerConfiguration.WALLET_EXPIRATION_CHANNEL
        private val EVENT_TYPE_REFERENCE = object : TypeReference<QueueEvent<WalletEvent>>() {}
    }

    private val logger = LoggerFactory.getLogger(WalletExpirationQueueConsumer::class.java)

    @ServiceActivator(inputChannel = INPUT_CHANNEL, outputChannel = "nullChannel")
    fun messageReceiver(
        @Payload payload: ByteArray,
        @Header(AzureHeaders.CHECKPOINTER) checkPointer: Checkpointer
    ): Mono<Unit> {
        return checkPointer
            .successWithLog()
            .then(
                BinaryData.fromBytes(payload).toObjectAsync(EVENT_TYPE_REFERENCE, azureSerializer)
            )
            .map { it.tracingInfo to it.data as WalletCreatedEvent }
            .doOnNext { (tracingInfo, walletCreatedEvent) ->
                logger.info(
                    "Parsed wallet expiration event from queue: [{}] with tracing info: [{}]",
                    walletCreatedEvent,
                    tracingInfo
                )
            }
            .doOnError { error ->
                logger.error("Exception processing wallet expiration event", error)
            }
            .thenReturn(Unit)
    }
}
