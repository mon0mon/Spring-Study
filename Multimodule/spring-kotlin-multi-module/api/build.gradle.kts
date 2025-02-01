plugins {
//    id("multi.spring-core.convention")
    alias(libs.plugins.multi.spring.core)
}

dependencies {
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
}
