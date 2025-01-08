package it.pagopa.wallet.eventdispatcher.streams

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import it.pagopa.generated.paymentwallet.eventdispatcher.server.model.DeploymentVersionDto
import it.pagopa.wallet.eventdispatcher.configuration.properties.RedisStreamEventControllerConfigs
import it.pagopa.wallet.eventdispatcher.service.InboundChannelAdapterLifecycleHandlerService
import it.pagopa.wallet.eventdispatcher.streams.commands.EventDispatcherGenericCommand
import it.pagopa.wallet.eventdispatcher.streams.commands.EventDispatcherReceiverCommand
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Service
import reactor.core.scheduler.Schedulers

/**
 * Redis Stream event consumer. This class handles all Redis Stream events performing requested
 * operation based on input event type
 */
@Service
class RedisStreamConsumer(
    @Autowired
    private val inboundChannelAdapterLifecycleHandlerService:
        InboundChannelAdapterLifecycleHandlerService,
    @Value("\${eventController.deploymentVersion}")
    @Autowired
    private val deploymentVersion: DeploymentVersionDto,
    @Autowired
    private val redisStreamReceiver:
        StreamReceiver<String, ObjectRecord<String, LinkedHashMap<*, *>>>,
    @Autowired private val redisStreamConf: RedisStreamEventControllerConfigs
) : ApplicationListener<ApplicationReadyEvent> {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun onApplicationEvent(applicationReadyEvent: ApplicationReadyEvent) {
        // register stream receiver
        logger.info("Starting Redis stream receiver")
        redisStreamReceiver
            .receive(
                StreamOffset.create(redisStreamConf.streamKey, ReadOffset.from(RecordId.of(0, 0)))
            )
            .subscribeOn(Schedulers.parallel())
            .subscribe(
                {
                    runCatching {
                            val event =
                                objectMapper.convertValue(
                                    it.value,
                                    EventDispatcherGenericCommand::class.java
                                )
                            processStreamEvent(event = event)
                        }
                        .onFailure { logger.error("Error processing redis stream event", it) }
                },
                { logger.error("Exception during redis stream processing", it) }
            )
    }

    fun processStreamEvent(event: EventDispatcherGenericCommand) {
        logger.info("Received event: {}", event)
        when (event) {
            is EventDispatcherReceiverCommand -> handleEventReceiverCommand(event)
        }
    }

    /** Handle event receiver command to start/stop receivers */
    private fun handleEventReceiverCommand(command: EventDispatcherReceiverCommand) {
        // current deployment version is targeted by command for exact version match or if command
        // does
        // not explicit a targeted version
        val currentDeploymentVersion = deploymentVersion
        val commandTargetVersion = command.version
        val isTargetedByCommand =
            commandTargetVersion == null || currentDeploymentVersion == commandTargetVersion
        logger.info(
            "Event dispatcher receiver command event received. Current deployment version: [{}], command deployment version: [{}] -> is this version targeted: [{}]",
            currentDeploymentVersion,
            commandTargetVersion ?: "ALL",
            isTargetedByCommand
        )
        if (isTargetedByCommand) {
            val commandToSend =
                when (command.receiverCommand) {
                    EventDispatcherReceiverCommand.ReceiverCommand.START -> "start"
                    EventDispatcherReceiverCommand.ReceiverCommand.STOP -> "stop"
                }
            inboundChannelAdapterLifecycleHandlerService.invokeCommandForAllEndpoints(commandToSend)
        } else {
            logger.info(
                "Current deployment version not targeted by command, command will not be processed"
            )
        }
    }
}
