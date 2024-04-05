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

    @Bean
    @InboundChannelAdapter(
        channel = "walletusagechannel",
        poller = Poller(fixedDelay = "1000", maxMessagesPerPoll = "10")
    )
    @EndpointId("storageQueueWalletUsageMessageSourceEndpoint")
    fun storageQueueWalletUsageMessageSource(
        storageQueueTemplate: StorageQueueTemplate,
        @Value("\${azurestorage.queues.walletusage.name}") walletUsageQueueName: String
    ) = StorageQueueMessageSource(walletUsageQueueName, storageQueueTemplate)
}
