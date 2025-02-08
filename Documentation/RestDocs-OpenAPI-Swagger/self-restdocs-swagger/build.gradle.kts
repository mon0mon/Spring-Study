import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    // REST Docs API Spec 플러그인 (테스트 스니펫 → OpenAPI 스펙)
    id("com.epages.restdocs-api-spec") version "0.19.4"
    // OpenAPI Generator Gradle 플러그인 (스펙 → 문서 생성)
    id("org.openapi.generator") version "7.11.0"
}

group = "xyz.mon0mon"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

extra["snippetsDir"] = file("build/generated-snippets")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4")
    testImplementation("com.epages:restdocs-api-spec-gradle-plugin:0.19.4")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
    inputs.dir(project.extra["snippetsDir"]!!)
    dependsOn(tasks.test)
}

// OpenAPI3 스펙 생성을 위한 설정
openapi3 {
    setServer("http://localhost:8080")
    title = "Sample API Documentation"
    description = "Spring REST Docs with OpenAPI and Swagger UI"
    version = "1.0.0"
    format = "yaml"  // JSON도 가능
}

//  openApiGenerate 태스크가 실행될 때 먼저 openapi3 태스크 실행
tasks.named("openApiGenerate") {
    dependsOn("openapi3")
}

//  OpenAPI Generator를 사용한 문서 생성
openApiGenerate {
    verbose = true
    generatorName.set("html2")  // html2 템플릿은 정적 HTML 문서를 생성합니다.
    inputSpec.set("${layout.buildDirectory.get()}/api-spec/openapi3.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated-docs")
    // 추가 설정 옵션 (필요에 따라)
    configOptions.set(mapOf("templateVersion" to "3.0.0"))
}

//  생성된 HTML2 문서를 복사
tasks.register<Copy>("copyOpenApiDocs") {
    dependsOn("openApiGenerate")
    from("${layout.buildDirectory.get()}/generated-docs")
    into(file("${layout.buildDirectory.get()}/assets/static/docs"))
    finalizedBy("injectOpenApiSpecToSwagger")
}

// 생성된 OpenAPI 3.0 문서를 build/assets/static/swagger 에 복사
tasks.register<Copy>("injectOpenApiSpecToSwagger") {
    from("${layout.projectDirectory}/swagger-ui")
    into(file("${layout.buildDirectory.get()}/assets/static/swagger"))

    from(file("${layout.buildDirectory.get()}/api-spec/openapi3.yaml"))
    into(file("${layout.buildDirectory.get()}/assets/static/swagger"))
}

// bootJar 또는 Jar 빌드시 Swagger UI 복사 태스크 실행
tasks.withType<BootJar> {
    dependsOn("copyOpenApiDocs", "injectOpenApiSpecToSwagger")
    // 만약 processResources에 복사한 결과가 자동 포함되지 않는다면 아래와 같이 명시적으로 추가할 수 있음.
    from(file("${layout.buildDirectory.get()}/assets")) {
        // jar 내부에서 기본적으로 리소스는 BOOT-INF/classes에 위치하므로 그대로 둡니다.
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        into("BOOT-INF/classes")
    }

    archiveFileName.set("application.jar")
}
