package xyz.mon0mon.springfeatureflags

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "features")
class ConfigProperties @ConstructorBinding constructor(
    val miner: MinerProperties,
    val ui: UIProperties
)

class MinerProperties(val experimental: Boolean)
class UIProperties(val cards: Boolean)
