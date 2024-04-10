package it.pagopa.wallet.eventdispatcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class WalletEventDispatcherApplication

fun main(args: Array<String>) {
    runApplication<WalletEventDispatcherApplication>(*args)
}
