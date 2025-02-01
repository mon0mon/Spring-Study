package xyz.mon0mon.springboottogglzaspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

private val log: Logger = LoggerFactory.getLogger(FeaturesAspect::class.java)

@Aspect
@Component
class FeaturesAspect {

    @Around("@within(featureAssociation) || @annotation(featureAssociation)")
    @Throws(Throwable::class)
    fun checkAspect(joinPoint: ProceedingJoinPoint, featureAssociation: FeatureAssociation): Any? {
        return if (featureAssociation.value.isActive()) { joinPoint.proceed() }
        else {
            log.info("Feature ${featureAssociation.value.name} is not enabled!")
            return null
        }
    }
}
