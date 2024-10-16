package it.pagopa.wallet.eventdispatcher.configuration

import it.pagopa.wallet.eventdispatcher.configuration.properties.WalletCDCConfiguration
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class WalletCDCClientConfiguration {

    @Bean(name = ["cdcEventHubClient"])
    fun cdcKafkaTemplate(
        walletCDCConfiguration: WalletCDCConfiguration
    ): KafkaTemplate<String, Any> {
        val configProps =
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to walletCDCConfiguration.bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to
                    ErrorHandlingDeserializer::class.java,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to JsonSerializer::class.java
            )
        val producerFactory = DefaultKafkaProducerFactory<String, Any>(configProps)
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun cdcTopicName(walletCDCConfiguration: WalletCDCConfiguration): String {
        return walletCDCConfiguration.cdcTopicName
    }
}
