package it.pagopa.wallet.eventdispatcher.configuration.properties

import java.util.*
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "redis-stream.event-controller")
class RedisStreamEventControllerConfigs(
    val streamKey: String,
    private val consumerGroupPrefix: String,
    private val consumerNamePrefix: String,
    val faiOnErrorCreatingConsumerGroup: Boolean
) {
    private val uniqueConsumerId = UUID.randomUUID().toString()
    val consumerGroup = "$consumerGroupPrefix-$uniqueConsumerId"
    val consumerName = "$consumerNamePrefix-$uniqueConsumerId"
}
