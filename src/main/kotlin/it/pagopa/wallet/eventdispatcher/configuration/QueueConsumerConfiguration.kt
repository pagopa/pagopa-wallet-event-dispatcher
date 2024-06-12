package it.pagopa.wallet.eventdispatcher.configuration

import com.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource
import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.EndpointId
import org.springframework.integration.annotation.InboundChannelAdapter
import org.springframework.integration.annotation.Poller

@Configuration
class QueueConsumerConfiguration {
    companion object {
        const val WALLET_USAGE_CHANNEL = "walletUsageChannel"
        const val WALLET_EXPIRATION_CHANNEL = "walletExpirationChannel"
    }

    @Bean
    @InboundChannelAdapter(
        channel = WALLET_USAGE_CHANNEL,
        poller = Poller(fixedDelay = "1000", maxMessagesPerPoll = "10")
    )
    @EndpointId("storageQueueWalletUsageMessageSourceEndpoint")
    fun storageQueueWalletUsageMessageSource(
        storageQueueTemplate: StorageQueueTemplate,
        @Value("\${azure.storage.queues.wallet.usage.name}") walletUsageQueueName: String
    ) = StorageQueueMessageSource(walletUsageQueueName, storageQueueTemplate)

    @Bean
    @InboundChannelAdapter(
        channel = WALLET_EXPIRATION_CHANNEL,
        poller =
            Poller(
                fixedDelay = "\${azure.storage.queues.wallet.expiration.polling.fixedDelay}",
                maxMessagesPerPoll =
                    "\${azure.storage.queues.wallet.expiration.polling.maxMessagePerPoll}"
            )
    )
    @EndpointId("storageQueueWalletExpirationMessageSourceEndpoint")
    fun storageQueueWalletExpirationMessageSource(
        storageQueueTemplate: StorageQueueTemplate,
        @Value("\${azure.storage.queues.wallet.expiration.name}") walletUsageQueueName: String
    ) = StorageQueueMessageSource(walletUsageQueueName, storageQueueTemplate)
}
