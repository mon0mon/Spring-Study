package xyz.mon0mon.springboottogglzaspect

import org.togglz.core.activation.SystemPropertyActivationStrategy
import org.togglz.core.annotation.ActivationParameter
import org.togglz.core.annotation.DefaultActivationStrategy
import org.togglz.core.annotation.EnabledByDefault
import org.togglz.core.annotation.Label
import org.togglz.core.context.FeatureContext

enum class MyFeatures {
    @Label("Employee Management Feature")
    @EnabledByDefault
    @DefaultActivationStrategy(
        id = SystemPropertyActivationStrategy.ID,
        parameters = [
            ActivationParameter(
                name = SystemPropertyActivationStrategy.PARAM_PROPERTY_NAME,
                value = "employee.feature"
            ),
            ActivationParameter(
                name = SystemPropertyActivationStrategy.PARAM_PROPERTY_VALUE,
                value = "true"
            )
        ]
    )
    EMPLOYEE_MANAGEMENT_FEATURE;

    fun isActive(): Boolean {
        return FeatureContext.getFeatureManager().isActive { name }
    }
}
