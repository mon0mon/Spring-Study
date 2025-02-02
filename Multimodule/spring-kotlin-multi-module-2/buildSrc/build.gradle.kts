plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

/**
 *     kotlin("jvm") version "1.9.25"
 *     kotlin("plugin.spring") version "1.9.25"
 *     id("org.springframework.boot") version "3.4.2"
 *     id("io.spring.dependency-management") version "1.1.7"
 *     kotlin("plugin.jpa") version "1.9.25"
 *     id("org.hibernate.orm") version "6.6.5.Final"
 *     id("org.graalvm.buildtools.native") version "0.10.4"
 */

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.9.25")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:1.9.25")
    implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin:1.9.25")
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:3.4.2")
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.7")
    implementation("org.hibernate.orm:org.hibernate.orm.gradle.plugin:6.6.5.Final")
    implementation("org.graalvm.buildtools:native-gradle-plugin:0.10.4")
}
