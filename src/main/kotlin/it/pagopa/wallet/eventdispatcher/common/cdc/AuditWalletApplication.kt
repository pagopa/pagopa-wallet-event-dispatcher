package it.pagopa.wallet.eventdispatcher.common.cdc

/** Data class that contains wallet application details for a log event */
data class AuditWalletApplication(
    val id: String,
    val status: String,
    val creationDate: String,
    val updateDate: String,
    val metadata: Map<String, String?>
) {}
