import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
//    id("multi.spring-core.convention")
    alias(libs.plugins.multi.spring.core)
}

tasks.withType<BootJar> {
    enabled = false
}

tasks.withType<Jar> {
    enabled = true
}
