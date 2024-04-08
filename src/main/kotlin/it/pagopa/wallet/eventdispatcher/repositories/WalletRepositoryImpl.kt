package it.pagopa.wallet.eventdispatcher.repositories

import it.pagopa.wallet.eventdispatcher.domain.WalletRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class WalletRepositoryImpl(private val mongoWalletRepository: MongoWalletRepository) :
    WalletRepository {
    override fun findById(walletId: String): Mono<WalletDocument> {
        return mongoWalletRepository.findById(walletId)
    }
}

@Repository interface MongoWalletRepository : ReactiveCrudRepository<WalletDocument, String>
