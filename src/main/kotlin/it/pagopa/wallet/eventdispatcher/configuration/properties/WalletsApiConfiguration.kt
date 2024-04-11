package it.pagopa.wallet.eventdispatcher.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "walletService")
data class WalletsApiConfiguration(
    val uri: String,
    val readTimeout: Int,
    val connectionTimeout: Int
)