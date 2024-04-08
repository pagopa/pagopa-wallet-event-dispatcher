package it.pagopa.wallet.eventdispatcher.common.queue

data class TracingInfo(
    val traceparent: String,
    val tracestate: String? = null,
    val baggage: String? = null,
)
