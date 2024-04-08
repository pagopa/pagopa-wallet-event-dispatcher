package it.pagopa.wallet.eventdispatcher.repositories

import it.pagopa.wallet.eventdispatcher.domain.WalletApplication
import it.pagopa.wallet.eventdispatcher.domain.WalletApplications
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document

@Document("payment-wallets")
data class WalletDocument(
    @Id var id: String,
    val userId: String,
    val status: String,
    val paymentMethodId: String,
    val contractId: String?,
    val validationOperationResult: String?,
    var validationErrorCode: String?,
    val applications: List<WalletApplication>,
    @Version var version: Int,
    @CreatedDate var creationDate: Instant,
    @LastModifiedDate var updateDate: Instant,
    val onboardingChannel: String
)