package it.pagopa.wallet.eventdispatcher.queues

import com.azure.spring.messaging.checkpoint.Checkpointer
import com.fasterxml.jackson.databind.ObjectMapper
import it.pagopa.wallet.eventdispatcher.configuration.SerializationConfiguration
import org.mockito.kotlin.mock

class WalletExpirationQueueConsumerTest {

    private val checkpointer: Checkpointer = mock()
    private val serializationConfiguration = SerializationConfiguration()
    private val objectMapper: ObjectMapper =
        serializationConfiguration
            .objectMapperBuilder().build()
    private val azureJsonSerializer = serializationConfiguration.azureJsonSerializer(objectMapper)
}