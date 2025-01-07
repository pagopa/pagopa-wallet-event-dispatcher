package it.pagopa.wallet.eventdispatcher.repositories.redis.commands

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

/** Event dispatcher generic command event class */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EventDispatcherReceiverCommand::class, name = "RECEIVER_COMMAND"),
)
sealed class EventDispatcherGenericCommand(
    val commandId: UUID = UUID.randomUUID(),
    val type: CommandType
) {
    enum class CommandType {
        RECEIVER_COMMAND
    }
}
