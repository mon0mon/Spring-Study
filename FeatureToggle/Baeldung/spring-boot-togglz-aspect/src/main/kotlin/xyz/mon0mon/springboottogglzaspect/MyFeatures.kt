package xyz.mon0mon.springboottogglzaspect

import org.togglz.core.annotation.Label
import org.togglz.core.context.FeatureContext

enum class MyFeatures {
    @Label("Employee Management Feature")
    EMPLOYEE_MANAGEMENT_FEATURE;

    fun isActive(): Boolean {
        return FeatureContext.getFeatureManager().isActive{ name }
    }
}
