package it.pagopa.wallet.eventdispatcher.configuration

import com.azure.messaging.eventhubs.EventHubClientBuilder
import com.azure.messaging.eventhubs.EventHubProducerClient
import it.pagopa.wallet.eventdispatcher.configuration.properties.WalletCDCConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WalletCDCClientConfiguration {

    @Bean(name = ["cdcEventHubClient"])
    fun cdcEventHubClient(walletCDCConfiguration: WalletCDCConfiguration): EventHubProducerClient {
        return EventHubClientBuilder()
            .connectionString(
                walletCDCConfiguration.connectionString,
                walletCDCConfiguration.eventHubName
            )
            .buildProducerClient()
    }
}
