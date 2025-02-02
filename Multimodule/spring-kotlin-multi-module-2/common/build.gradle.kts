import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("multi.spring-core.convention")
}

tasks.withType<BootJar> {
    enabled = false
}

tasks.withType<Jar> {
    enabled = true
}
