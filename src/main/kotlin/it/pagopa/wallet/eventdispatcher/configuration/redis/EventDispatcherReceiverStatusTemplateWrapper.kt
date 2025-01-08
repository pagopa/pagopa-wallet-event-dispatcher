package it.pagopa.wallet.eventdispatcher.configuration.redis

import it.pagopa.wallet.eventdispatcher.repositories.redis.bean.ReceiversStatus
import it.pagopa.wallet.eventdispatcher.utils.RedisTemplateWrapper
import java.time.Duration
import org.springframework.data.redis.core.RedisTemplate

/** Redis template wrapper used to handle event receiver statuses */
class EventDispatcherReceiverStatusTemplateWrapper(
    redisTemplate: RedisTemplate<String, ReceiversStatus>,
    defaultEntitiesTTL: Duration
) : RedisTemplateWrapper<ReceiversStatus>(redisTemplate, "receiver-status", defaultEntitiesTTL) {
    override fun getKeyFromEntity(value: ReceiversStatus): String = value.consumerInstanceId
}
