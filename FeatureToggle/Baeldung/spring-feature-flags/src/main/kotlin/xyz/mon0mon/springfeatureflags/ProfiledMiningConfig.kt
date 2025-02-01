package xyz.mon0mon.springfeatureflags

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Using Spring Profile to Apply Feature Toggle
 */
@Configuration
class ProfiledMiningConfig {
    @Bean
    @Profile("!experimental-miner")
    fun defaultMiner(): BitcoinMiner {
        return DefaultBitcoinMiner()
    }

    @Bean
    @Profile("experimental-miner")
    fun experimentalMiner(): BitcoinMiner {
        return ExperimentalBitcoinMiner()
    }
}

interface BitcoinMiner {}

class DefaultBitcoinMiner: BitcoinMiner

class ExperimentalBitcoinMiner: BitcoinMiner
