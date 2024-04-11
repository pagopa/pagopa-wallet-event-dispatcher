package it.pagopa.wallet.eventdispatcher.domain

import java.util.UUID

class WalletUpdateUsageError(val walletId: UUID) : Exception()
