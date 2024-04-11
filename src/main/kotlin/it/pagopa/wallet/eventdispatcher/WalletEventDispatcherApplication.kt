package it.pagopa.wallet.eventdispatcher

import it.pagopa.wallet.eventdispatcher.configuration.properties.WalletsApiConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.integration.config.EnableIntegration
import reactor.core.publisher.Hooks

@SpringBootApplication
@EnableIntegration
@EnableConfigurationProperties(WalletsApiConfiguration::class)
class WalletEventDispatcherApplication

fun main(args: Array<String>) {
    Hooks.enableAutomaticContextPropagation()
    runApplication<WalletEventDispatcherApplication>(*args)
}
