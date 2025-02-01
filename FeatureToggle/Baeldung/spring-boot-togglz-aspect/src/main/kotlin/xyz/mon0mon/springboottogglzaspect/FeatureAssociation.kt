package xyz.mon0mon.springboottogglzaspect

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class FeatureAssociation(val value: MyFeatures)
