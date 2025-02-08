# Spring RestDocs Open API 문서 활용

<!-- TOC -->
* [Spring RestDocs Open API 문서 활용](#spring-restdocs-open-api-문서-활용)
  * [의존성](#의존성)
  * [1. Spring RestDocs로 Open API 문서 추출](#1-spring-restdocs로-open-api-문서-추출)
  * [2. Open API 3.0 Spec을 기반으로 HTML 문서 생성](#2-open-api-30-spec을-기반으로-html-문서-생성)
  * [3. Open API 3.0 Spec을 기반으로 Swagger-UI 적용](#3-open-api-30-spec을-기반으로-swagger-ui-적용)
  * [4. BootJar 빌드 시, HTML 파일 및 Swagger-UI 포함되도록 설정](#4-bootjar-빌드-시-html-파일-및-swagger-ui-포함되도록-설정)
    * [실행시 결과](#실행시-결과)
  * [5. Spring Security로 문서파일 접근제어 설정](#5-spring-security로-문서파일-접근제어-설정)
    * [실행 시 결과](#실행-시-결과)
  * [참고 문서](#참고-문서)
<!-- TOC -->

## 의존성
```
// build.gradle.kts

plugins {
    // REST Docs API Spec 플러그인 (테스트 스니펫 → OpenAPI 스펙)
    id("com.epages.restdocs-api-spec") version "0.19.4"
    // OpenAPI Generator Gradle 플러그인 (스펙 → 문서 생성)
    id("org.openapi.generator") version "7.11.0"
}

// ...

dependencies {
    //  Spring RestDocs
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    //  OpenAPI Spec 변환 의존성
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4")
    //  OpenAPI Gradle Task 실행 옵션 추가 (openapi tools)
    testImplementation("com.epages:restdocs-api-spec-gradle-plugin:0.19.4")
}
```

## 1. Spring RestDocs로 Open API 문서 추출
- Spring RestDocs 의존성을 추가한 이후에, Test 실행 시 `build/generated-snippets`에 API snippet이 생성됨<br>
`./gradlew test`

- 생성될 Open API Spec을 다음과 같이 설정
```
// build.gradle.kts

// OpenAPI3 스펙 생성을 위한 설정
openapi3 {
    setServer("http://localhost:8080")
    title = "Sample API Documentation"
    description = "Spring REST Docs with OpenAPI and Swagger UI"
    version = "1.0.0"
    format = "yaml"  // JSON도 가능
}
```

- 생성된 API snippet을 이용해서, Open API 문서로 변환<br>
`./gradlew openapi3`
- 빌드된 API 스펙 파일은 `build/api-spec/openapi3.yaml`에 저장됨

## 2. Open API 3.0 Spec을 기반으로 HTML 문서 생성
```
// build.gradle.kts

//  OpenAPI Generator를 사용한 문서 생성
openApiGenerate {
    verbose = true
    generatorName.set("html2")
    inputSpec.set("${layout.buildDirectory.get()}/api-spec/openapi3.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated-docs")
    configOptions.set(mapOf("templateVersion" to "3.0.0"))
}
```
- generator 종류는 [공식문서](https://openapi-generator.tech/docs/generators#documentation-generators) 참고<br>
  `./gradlew openApiGenerate `

## 3. Open API 3.0 Spec을 기반으로 Swagger-UI 적용
- [공식 가이드](https://swagger.io/docs/open-source-tools/swagger-ui/usage/installation/#plain-old-htmlcssjs-standalone) 내용에 따라서 Swagger-UI 배포파일 추가
  - [Swagger-UI Github](https://github.com/swagger-api/swagger-ui)에서 최신 배포판 다운로드
  - 압축을 푼 이후에, 내부에 있는 dist 폴더만 현재 프로젝트로 이동
  - `swagger-initializer.js`에서 `url: "./openapi3.yaml"`로 변경

- Gradle에 다음 작업 추가
```
// build.gradle.kts

// 생성된 OpenAPI 3.0 문서를 src/main/resources/static/docs 에 복사
tasks.register<Copy>("injectOpenApiSpecToSwagger") {
    from("${layout.projectDirectory}/swagger-ui")
    into(file("${layout.buildDirectory.get()}/assets/static/swagger"))

    from(file("${layout.buildDirectory.get()}/api-spec/openapi3.yaml"))
    into(file("${layout.buildDirectory.get()}/assets/static/swagger"))
}
```
`./gradlew injectOpenApiSpecToSwagger`

## 4. BootJar 빌드 시, HTML 파일 및 Swagger-UI 포함되도록 설정

- BootJar 빌드시, Swagger와 HTML 파일이 포함되도록 작업 추가
```
// build.gradle.kts

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
```

- 작업 실행 순서
  1. test 실행
  2. openapi3 (test에서 빌드된 snippet들을 Open API Spec으로 변환하는 작업)
  3. openApiGenerate (Open Api Spec을 기반으로 HTML 문서를 생성하는 작업)
  4. copyOpenApiDocs (생성된 HTML 문서를 빌드 디렉토리로 저장)
  5. injectOpenApiSpecToSwagger (Open Api Spec을 Swagger 폴더로 복사)
  6. bootJar

### 빌드파일 구조
```
├───BOOT-INF
│   ├───classes
│   │   ├───META-INF
│   │   ├───static
│   │   │   ├───docs
│   │   │   │   └───.openapi-generator
│   │   │   └───swagger
│   │   ├───templates
│   │   └───xyz
│   │       └───mon0mon
│   │           └───selfrestdocsswagger
│   └───lib
├───META-INF
└───org
```

### 실행시 결과

#### Docs
![OpenAPI_HTML.png](img%2FOpenAPI_HTML.png)
- [http://localhost:8080/docs/index.html](http://localhost:8080/docs/index.html)

#### Swagger
![OpenAPI_Swagger.png](img%2FOpenAPI_Swagger.png)
- [http://localhost:8080/swagger/index.html](http://localhost:8080/swagger/index.html)

## 5. Spring Security로 문서파일 접근제어 설정

- Spring Security 의존성 추가 후 설정 파일 추가

```
//  SecurityConfig

@Bean
@Order(1)
fun swaggerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
        // Swagger 페이지에만 적용할 경로 설정
        .securityMatcher("/swagger/**", "/docs/**", "/login", "/error", "/logout", "/default-ui.css")
        .authorizeHttpRequests { auth ->
            auth.requestMatchers("/swagger/**", "/docs/**", "/logout").authenticated()
            auth.anyRequest().permitAll()
        }
        //  폼 로그인 사용
        .formLogin {  }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
        .csrf { it.disable() }
        .build()
}

@Bean
@Order(2)
//...

@Bean
fun swaggerUserDetailsService(): UserDetailsService {
    val user = User.builder()
        .username("user")
        .password("{noop}1234")
        .roles("SWAGGER")
        .build()
    return InMemoryUserDetailsManager(user)
}
```

- SecurityFilterChain을 다중으로 두어서, API 인증과 API 문서 인증을 별도로 분리
  - API 문서 인증 : FormLogin 방식 (Basic Auth의 경우, 인증정보가 브라우저에 저장되어 Form Login/Session 방식 선택)

### 실행 시 결과
![SpringSecurityApplied.gif](img%2FSpringSecurityApplied.gif)


## 참고 문서
- [Kurly Tech Blog](https://helloworld.kurly.com/blog/spring-rest-docs-guide/)
- [OpenAPI Generator](https://github.com/OpenAPITools/openapi-generator)
- [Swagger UI Docs](https://swagger.io/docs/open-source-tools/swagger-ui/usage/installation/#plain-old-htmlcssjs-standalone)
- [Spring Security](https://docs.spring.io/spring-security/reference/servlet/configuration/kotlin.html#_securityfilterchain_endpoints)

