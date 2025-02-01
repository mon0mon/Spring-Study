package xyz.mon0mon.springfeatureflags

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CustomPropsMiningConfig {
    @Bean
    @ConditionalOnProperty(name = ["features.miner.experimental"], matchIfMissing = true)
    fun defaultMiner(): BitcoinMiner = DefaultBitcoinMiner()

    @Bean
    @ConditionalOnProperty(name = ["features.miner.experimental"])
    fun experimentMiner(): BitcoinMiner = ExperimentalBitcoinMiner()
}
