package it.pagopa.wallet.eventdispatcher.services

import it.pagopa.wallet.eventdispatcher.repositories.MongoWalletRepository
import it.pagopa.wallet.eventdispatcher.repositories.WalletDocument
import it.pagopa.wallet.eventdispatcher.repositories.WalletRepositoryImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.Instant
import java.util.*

class WalletUsageServiceTest {

    private val walletRepository: MongoWalletRepository = mock()
    private val walletUsageService = WalletUsageService(WalletRepositoryImpl(walletRepository))

    @BeforeEach
    fun setUp() {
        reset(walletRepository)
    }

    @Test
    fun `should update last usage by existing WalletId`() {
        val wallet = randomWallet()
        val clientId = UUID.randomUUID().toString()
        given { walletRepository.findById(any<String>()) }.willAnswer { Mono.just(wallet) }

        walletUsageService.updateLastUsage(wallet.id, clientId).test().assertNext {
            verify(walletRepository, times(1)).findById(wallet.id)
        }
    }

    private fun randomWallet(): WalletDocument =
        WalletDocument(
            id = UUID.randomUUID().toString(),
            userId = UUID.randomUUID().toString(),
            status = "VALIDATED",
            paymentMethodId = UUID.randomUUID().toString(),
            contractId = UUID.randomUUID().toString(),
            validationErrorCode = null,
            validationOperationResult = null,
            applications = emptyList(),
            version = 1,
            creationDate = Instant.now(),
            updateDate = Instant.now(),
            onboardingChannel = "IO"
        )
}
