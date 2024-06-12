package it.pagopa.wallet.eventdispatcher.common.serialization

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import it.pagopa.wallet.eventdispatcher.common.serialization.WalletEventMixin.Companion.WALLET_EXPIRED_TYPE
import it.pagopa.wallet.eventdispatcher.common.serialization.WalletEventMixin.Companion.WALLET_USED_TYPE
import it.pagopa.wallet.eventdispatcher.domain.WalletCreatedEvent
import it.pagopa.wallet.eventdispatcher.domain.WalletUsedEvent

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = WalletUsedEvent::class, name = WALLET_USED_TYPE),
    JsonSubTypes.Type(value = WalletCreatedEvent::class, name = WALLET_EXPIRED_TYPE)
)
class WalletEventMixin {
    companion object {
        const val WALLET_USED_TYPE = "WalletUsed"
        const val WALLET_EXPIRED_TYPE = "WalletExpired"
    }
}
