package xyz.mon0mon.springboottogglzaspect

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.togglz.core.spi.FeatureProvider
import org.togglz.kotlin.EnumClassFeatureProvider

@Configuration
class ToggleConfiguration {
    @Bean
    fun featureProvider(): FeatureProvider {
        return EnumClassFeatureProvider(MyFeatures::class.java)
    }
}
