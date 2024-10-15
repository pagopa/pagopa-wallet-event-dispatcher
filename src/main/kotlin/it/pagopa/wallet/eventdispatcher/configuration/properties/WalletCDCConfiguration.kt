package it.pagopa.wallet.eventdispatcher.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "azure.eventhub")
data class WalletCDCConfiguration(val connectionString: String, val eventHubName: String)
