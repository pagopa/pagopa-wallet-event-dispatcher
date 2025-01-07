package it.pagopa.wallet.eventdispatcher.configuration.redis

import it.pagopa.wallet.eventdispatcher.repositories.redis.RedisTemplateWrapper
import it.pagopa.wallet.eventdispatcher.repositories.redis.commands.EventDispatcherReceiverCommand
import java.time.Duration
import org.springframework.data.redis.core.RedisTemplate

/** Redis command template wrapper, used to write events to Redis stream */
class EventDispatcherCommandsTemplateWrapper(
    redisTemplate: RedisTemplate<String, EventDispatcherReceiverCommand>,
    defaultEntitiesTTL: Duration
) :
    RedisTemplateWrapper<EventDispatcherReceiverCommand>(
        redisTemplate,
        "eventDispatcher",
        defaultEntitiesTTL
    ) {
    override fun getKeyFromEntity(value: EventDispatcherReceiverCommand): String =
        value.commandId.toString()
}
