package it.pagopa.wallet.eventdispatcher.utils

import io.opentelemetry.api.common.AttributeKey

object TracingKeys {

    val PATCH_STATE_TRIGGER_KEY = AttributeKey.stringKey("wallet.patch.state.trigger")

    enum class WalletPatchTriggerKind {
        WALLET_EXPIRE
    }
}
