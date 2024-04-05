package it.pagopa.wallet.eventdispatcher.common.queue

import it.pagopa.wallet.eventdispatcher.domain.WalletEvent

// TODO: add trace info
data class QueueEvent<T : WalletEvent>(val data: T)
