package it.pagopa.wallet.eventdispatcher.common.cdc

open class LoggingEvent(
    open val id: String,
    open val timestamp: String
) {}

data class WalletDeletedEvent(
    override val id: String,
    override val timestamp: String,
    val walletId: String
) : LoggingEvent(id, timestamp)

data class WalletApplicationsUpdatedEvent(
    override val id: String,
    override val timestamp: String,
    val walletId: String,
    val updatedApplications: List<AuditWalletApplication>
) : LoggingEvent(id, timestamp)

data class WalletOnboardCompletedEvent(
    override val id: String,
    override val timestamp: String,
    val walletId: String,
    val auditWallet: AuditWallet
) : LoggingEvent(id, timestamp)
