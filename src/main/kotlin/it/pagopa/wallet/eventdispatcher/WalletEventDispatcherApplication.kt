package it.pagopa.wallet.eventdispatcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.integration.config.EnableIntegration

@SpringBootApplication @EnableIntegration class WalletEventDispatcherApplication

fun main(args: Array<String>) {
    runApplication<WalletEventDispatcherApplication>(*args)
}
