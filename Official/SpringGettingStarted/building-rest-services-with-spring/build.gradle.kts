import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
    }

    group = "com.mon0mon"
    version = "0.0.1-SNAPSHOT"

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-web")

        runtimeOnly("com.h2database:h2")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

project("nonrest") {
    dependencies {
    }

    tasks.getByName<Jar>("jar") { enabled = false }
    tasks.getByName<BootJar>("bootJar") { enabled = true }
}

project("rest") {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-hateoas")
    }

    tasks.getByName<Jar>("jar") { enabled = false }
    tasks.getByName<BootJar>("bootJar") { enabled = true }
}

project("evolution") {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-hateoas")
    }

    tasks.getByName<Jar>("jar") { enabled = false }
    tasks.getByName<BootJar>("bootJar") { enabled = true }
}

project("links") {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-hateoas")
    }

    tasks.getByName<Jar>("jar") { enabled = false }
    tasks.getByName<BootJar>("bootJar") { enabled = true }
}

