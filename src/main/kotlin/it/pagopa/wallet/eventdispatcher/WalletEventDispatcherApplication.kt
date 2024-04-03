package it.pagopa.wallet.eventdispatcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class EventDispatcherApplication

fun main(args: Array<String>) {
    runApplication<EventDispatcherApplication>(*args)
}
