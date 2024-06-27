package it.pagopa.wallet.eventdispatcher.queues

import com.azure.core.util.BinaryData
import com.azure.core.util.serializer.JsonSerializerProvider
import com.azure.core.util.serializer.TypeReference
import com.azure.spring.messaging.AzureHeaders
import com.azure.spring.messaging.checkpoint.Checkpointer
import it.pagopa.generated.wallets.model.ClientId
import it.pagopa.wallet.eventdispatcher.common.queue.QueueEvent
import it.pagopa.wallet.eventdispatcher.configuration.QueueConsumerConfiguration
import it.pagopa.wallet.eventdispatcher.domain.WalletEvent
import it.pagopa.wallet.eventdispatcher.domain.WalletUsedEvent
import it.pagopa.wallet.eventdispatcher.service.WalletUsageService
import it.pagopa.wallet.eventdispatcher.utils.Tracing
import org.slf4j.LoggerFactory
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class WalletUsageQueueConsumer(
    private val walletUsageService: WalletUsageService,
    private val tracing: Tracing,
    azureJsonSerializer: JsonSerializerProvider
) {

    private val azureSerializer = azureJsonSerializer.createInstance()

    companion object {
        const val INPUT_CHANNEL = QueueConsumerConfiguration.WALLET_USAGE_CHANNEL
        private val EVENT_TYPE_REFERENCE = object : TypeReference<QueueEvent<WalletEvent>>() {}
    }

    private val logger = LoggerFactory.getLogger(WalletUsageQueueConsumer::class.java)
    private val consumerSpanName = WalletUsageQueueConsumer::class.java.simpleName

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
            .flatMap {
                tracing.traceMonoWithRemoteSpan(consumerSpanName, it.tracingInfo) {
                    handleWalletUsedEvent(it.data as WalletUsedEvent)
                }
            }
            .doOnError { error ->
                logger.error("Unexpected failure during event processing", error)
            }
    }

    private fun handleWalletUsedEvent(event: WalletUsedEvent): Mono<Unit> {
        return walletUsageService.updateWalletUsage(
            event.walletId,
            ClientId.fromValue(event.clientId),
            event.creationDate
        )
    }
}
