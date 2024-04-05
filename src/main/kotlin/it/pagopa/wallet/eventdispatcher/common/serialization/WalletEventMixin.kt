package it.pagopa.wallet.eventdispatcher.common.serialization

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import it.pagopa.wallet.eventdispatcher.domain.WalletUsed

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_class")
@JsonSubTypes(
    JsonSubTypes.Type(
        value = WalletUsed::class,
        name = "it.pagopa.wallet.eventdispatcher.domain.WalletUsed"
    )
)
class WalletEventMixin