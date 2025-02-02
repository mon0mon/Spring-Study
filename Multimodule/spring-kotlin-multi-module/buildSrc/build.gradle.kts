plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.jvm.gradle)
    implementation(libs.jpa.gradle)
    implementation(libs.spring.gradle)
    implementation(libs.dependency.management.gradle)
    implementation(libs.springboot.gradle)
}
