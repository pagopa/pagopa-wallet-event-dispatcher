package it.pagopa.wallet.eventdispatcher.queues

import com.azure.core.util.BinaryData
import com.azure.core.util.serializer.JsonSerializerProvider
import com.azure.core.util.serializer.TypeReference
import com.azure.spring.messaging.AzureHeaders
import com.azure.spring.messaging.checkpoint.Checkpointer
import it.pagopa.wallet.eventdispatcher.domain.WalletEvent
import it.pagopa.wallet.eventdispatcher.common.queue.QueueEvent
import org.slf4j.LoggerFactory
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service("WalletUsageQueueConsumer")
class WalletUsageQueueConsumer(azureJsonSerializer: JsonSerializerProvider) {

    private val azureSerializer = azureJsonSerializer.createInstance()

    companion object {
        const val INPUT_CHANNEL = "walletusagechannel"
        private val EVENT_TYPE_REFERENCE = object : TypeReference<QueueEvent<WalletEvent>>() {}
    }

    private val logger = LoggerFactory.getLogger(WalletUsageQueueConsumer::class.java)

    @ServiceActivator(inputChannel = INPUT_CHANNEL, outputChannel = "nullChannel")
    fun messageReceiver(
        @Payload payload: ByteArray,
        @Header(AzureHeaders.CHECKPOINTER) checkPointer: Checkpointer
    ): Mono<Unit> {
        println(BinaryData.fromBytes(payload).toString())
        return BinaryData.fromBytes(payload)
            .toObjectAsync(EVENT_TYPE_REFERENCE, azureSerializer)
            .doOnNext { logger.info("Received event {}", it) }
            .then(
                checkPointer
                    .success()
                    .doOnSuccess { logger.info("Checkpoint successfully") }
                    .doOnError { logger.error("Error performing checkpoint", it) },
            )
            .map {}
    }
}