package it.pagopa.wallet.eventdispatcher.configuration

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenTelemetryConfiguration {

    @Bean fun agentOpenTelemetry(): OpenTelemetry = GlobalOpenTelemetry.get()

    @Bean
    fun tracer(openTelemetry: OpenTelemetry, applicationContext: ApplicationContext): Tracer =
        openTelemetry.getTracer(applicationContext.applicationName)
}
