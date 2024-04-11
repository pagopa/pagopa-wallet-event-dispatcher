package it.pagopa.wallet.eventdispatcher.common.serialization

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import it.pagopa.wallet.eventdispatcher.common.serialization.WalletEventMixin.Companion.WALLET_USED_TYPE
import it.pagopa.wallet.eventdispatcher.domain.WalletUsed

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(JsonSubTypes.Type(value = WalletUsed::class, name = WALLET_USED_TYPE))
class WalletEventMixin {
    companion object {
        const val WALLET_USED_TYPE = "WalletUsed"
    }
}
