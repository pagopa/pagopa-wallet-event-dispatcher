package it.pagopa.wallet.eventdispatcher.configuration

import it.pagopa.wallet.eventdispatcher.configuration.properties.WalletCDCConfiguration
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import reactor.kafka.sender.SenderOptions

@Configuration
class WalletCDCClientConfiguration {

    @Bean(name = ["cdcEventHubClient"])
    fun cdcKafkaTemplate(
        walletCDCConfiguration: WalletCDCConfiguration
    ): ReactiveKafkaProducerTemplate<String, Any> {
        val configProps =
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to walletCDCConfiguration.bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to
                    ErrorHandlingDeserializer::class.java,
                "sasl.jaas.config" to
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"\$ConnectionString\" password=\"${walletCDCConfiguration.connectionString}\";",
                "sasl.mechanism" to "PLAIN",
                "security.protocol" to "SASL_SSL"
            )

        val senderOptions = SenderOptions.create<String, Any>(configProps)
        return ReactiveKafkaProducerTemplate(senderOptions)
    }

    @Bean
    fun cdcTopicName(walletCDCConfiguration: WalletCDCConfiguration): String {
        return walletCDCConfiguration.cdcTopicName
    }
}
