# Spring Multi Module 초기 설정

## 1. 필요한 모듈 생성
```
+---.gradle
+---.idea
+---api (Web Module)
+---batch (Batch Module)
+---build
+---buildSrc (Gradle Setting)
+---common (Common Module)
+---gradle
```

## 2. buildSrc에서 공통 Gradle 설정

### A. build.gradle.kts 수정
```kotlin
plugins {
    `kotlin-dsl`
}
```

### B. 모듈별로 적용할 컨벤션 파일 추가
- convention은 모듈에서 plugin 형식으로 적용
  - id(...)
```kotlin
plugins {
    id("multi.spring-core.convention")
}
```
<br>

- convention 사용 시, plugins 스코프에서 버전 설정 불가
  - buildSrc/build.gradle.kts에 pre compiled된 script plugin을 추가 해야함
```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.9.25")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:1.9.25")
    implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin:1.9.25")
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:3.4.2")
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.7")
}
```

```kotlin
// *.*.convention.gradle.kts
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.jpa")
}

// ...
```

<details>
<summary>kotlin 플러그인 implentation 찾는 방법</summary>

<!-- Blank -->
- 내부적으로 id("...")을 치환해주는 역할
- build.gradle.kts에 들어가면, 해당 id를 찾을 수 있음
![image.png](images%2Fimage.png)
- 해당 내용을 아래 URL로 검색
  - [Gradle Plugin Search](https://plugins.gradle.org/search)
  - [Kotlin Jvm](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.jvm)
![image2.png](images%2Fimage2.png)
</details>

## 3. 버전 카탈로그 설정

### A. root gradle 폴더에 libs.versions.toml 추가
```toml
[versions]
kotlin = "1.9.25"
spring = "3.4.2"
dependency-management = "1.1.7"

[libraries]
kotlin-jvm-gradle = { module = "org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin", version.ref = "kotlin" }
spring-gradle = { module = "org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin", version.ref = "kotlin" }
springboot-gradle = { module = "org.springframework.boot:org.springframework.boot.gradle.plugin", version.ref = "spring" }
dependency-management-gradle = { module = "io.spring.dependency-management:io.spring.dependency-management.gradle.plugin", version.ref = "dependency-management" }
jpa-gradle = { module = "org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin", version.ref = "kotlin" }

[bundles]

[plugins]
multi-spring-core = { id = "multi.spring-core.convention" }
multi-spring-batch = { id = "multi.spring-batch.convention" }
```

### B. buildSrc에 settings.gradle.kts 추가
```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
```

### C. 모듈 별 build.gradle.kts 수정
```diff
plugins {
--    id("multi.spring-core.convention")
++    alias(libs.plugins.multi.spring.core)
--    id("multi.spring-batch.convention")
++    alias(libs.plugins.multi.spring.batch)
}
```

## 4. common 모듈에서 BootJar 비활성화
```kotlin
tasks.withType<BootJar> {
    enabled = false
}

tasks.withType<Jar> {
    enabled = true
}
```
