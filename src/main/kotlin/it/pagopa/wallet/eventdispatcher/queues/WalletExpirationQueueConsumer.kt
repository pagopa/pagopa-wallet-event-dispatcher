package it.pagopa.wallet.eventdispatcher.queues

import com.azure.core.util.BinaryData
import com.azure.core.util.serializer.JsonSerializerProvider
import com.azure.core.util.serializer.TypeReference
import com.azure.spring.messaging.AzureHeaders
import com.azure.spring.messaging.checkpoint.Checkpointer
import it.pagopa.generated.wallets.model.WalletStatus
import it.pagopa.generated.wallets.model.WalletStatusErrorPatchRequest
import it.pagopa.generated.wallets.model.WalletStatusErrorPatchRequestDetails
import it.pagopa.wallet.eventdispatcher.api.WalletsApi
import it.pagopa.wallet.eventdispatcher.common.queue.QueueEvent
import it.pagopa.wallet.eventdispatcher.configuration.QueueConsumerConfiguration
import it.pagopa.wallet.eventdispatcher.domain.WalletCreatedEvent
import it.pagopa.wallet.eventdispatcher.domain.WalletEvent
import it.pagopa.wallet.eventdispatcher.utils.Tracing
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class WalletExpirationQueueConsumer(
    azureJsonSerializer: JsonSerializerProvider,
    private val walletsApi: WalletsApi,
    private val tracing: Tracing,
) {

    private val azureSerializer = azureJsonSerializer.createInstance()

    companion object {
        const val INPUT_CHANNEL = QueueConsumerConfiguration.WALLET_EXPIRATION_CHANNEL
        private val EVENT_TYPE_REFERENCE = object : TypeReference<QueueEvent<WalletEvent>>() {}
    }

    private val logger = LoggerFactory.getLogger(WalletExpirationQueueConsumer::class.java)
    private val consumerSpanName = WalletExpirationQueueConsumer::class.java.simpleName

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
                    handleWalletCreatedEvent(it.data as WalletCreatedEvent)
                }
            }
            .doOnError { error ->
                logger.error("Exception processing wallet expiration event", error)
            }
            .thenReturn(Unit)
    }

    private fun handleWalletCreatedEvent(event: WalletCreatedEvent): Mono<Unit> {
        val walletId = event.walletId
        val walletCreationDate = event.creationDate
        logger.info(
            "Processing wallet expiration event for wallet with id: [{}], created at: [{}]",
            walletId,
            walletCreationDate
        )
        return walletsApi.updateWalletStatus(
            walletId = UUID.fromString(walletId),
            walletStatusPatchRequest =
                WalletStatusErrorPatchRequest()
                    .status(WalletStatus.ERROR.toString())
                    .details(
                        WalletStatusErrorPatchRequestDetails()
                            .reason("Wallet expired. Creation date: $walletCreationDate")
                    )
        )
    }
}
