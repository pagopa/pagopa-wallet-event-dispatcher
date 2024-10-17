package it.pagopa.wallet.eventdispatcher.common.cdc

open class LoggingEvent(
    open val id: String,
    open val timestamp: String,
    open val type: String = LoggingEvent::class.java.simpleName
) {}

data class WalletDeletedEvent(
    override val id: String,
    override val timestamp: String,
    override val type: String = WalletDeletedEvent::class.java.simpleName,
    val walletId: String
) : LoggingEvent(id, timestamp)

data class WalletApplicationsUpdatedEvent(
    override val id: String,
    override val timestamp: String,
    override val type: String = WalletApplicationsUpdatedEvent::class.java.simpleName,
    val walletId: String,
    val updatedApplications: List<AuditWalletApplication>
) : LoggingEvent(id, timestamp)

data class WalletOnboardCompletedEvent(
    override val id: String,
    override val timestamp: String,
    override val type: String = WalletOnboardCompletedEvent::class.java.simpleName,
    val walletId: String,
    val auditWallet: AuditWallet
) : LoggingEvent(id, timestamp)
