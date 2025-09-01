// build.gradle 파일은 프로젝트를 만들고(빌드) 실행하는 데 필요한 모든 설정을 담고 있는 '설계도'와 같습니다.
// 이 파일 덕분에 개발자는 매번 수동으로 설정하지 않고, 정해진 방식대로 프로그램을 만들 수 있습니다.

// plugins: 이 프로젝트에 어떤 '추가 기능'들을 사용할지 정하는 곳입니다.
// 예를 들어, 'org.springframework.boot'는 스프링 부트라는 기술을 쉽게 사용하게 해주는 기능입니다.
plugins {
    // 'java' 플러그인은 이 프로젝트가 자바(Java) 언어로 만들어졌다는 것을 알려줍니다.
    id 'java'
    // 'org.springframework.boot' 플러그인은 스프링 부트(Spring Boot) 프레임워크를 사용하여 웹 애플리케이션을 쉽게 만들 수 있도록 도와줍니다.
    // version '3.5.5'는 스프링 부트의 특정 버전을 사용하겠다는 의미입니다.
    id 'org.springframework.boot' version '3.S.5'
    // 'io.spring.dependency-management' 플러그인은 프로젝트에 필요한 여러 부품(라이브러리)들의 버전을
    // 스프링 부트가 추천하는 가장 안정적인 조합으로 자동으로 관리해주는 역할을 합니다.
    id 'io.spring.dependency-management' version '1.1.7'
}

// group: 이 프로젝트가 속한 그룹이나 회사를 나타내는 고유한 이름입니다. 보통 웹사이트 주소를 거꾸로 사용합니다.
group = 'kr.it.pullit'
// version: 이 프로젝트의 현재 버전을 나타냅니다. '0.0.1-SNAPSHOT'은 아직 개발 중인 첫 번째 버전이라는 의미입니다.
version = '0.0.1-SNAPSHOT'
// description: 이 프로젝트에 대한 간단한 설명입니다.
description = 'pullit'

// java: 자바 언어와 관련된 설정을 하는 곳입니다.
java {
    toolchain {
        // 이 프로젝트는 자바 17 버전을 사용해서 만들어졌다는 것을 명시합니다.
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// configurations: 프로젝트의 코드를 컴퓨터가 이해하는 언어로 바꾸는 과정(컴파일)에 대한 세부 설정을 하는 곳입니다.
configurations {
    // 'compileOnly'는 코드를 작성하고 컴파일할 때만 필요한 부품(라이브러리)을 의미합니다.
    compileOnly {
        // 'annotationProcessor'도 컴파일 시에만 필요한데, 이 둘을 함께 묶어서 관리하겠다는 설정입니다.
        extendsFrom annotationProcessor
    }
}

// repositories: 프로젝트에 필요한 부품(라이브러리)들을 어디서 다운로드할지 정하는 '창고' 목록입니다.
repositories {
    // 'mavenCentral()'은 전 세계 개발자들이 만든 수많은 공개 라이브러리들이 모여있는 가장 대표적인 중앙 창고입니다.
    mavenCentral()
}

// ext: 이 build.gradle 파일 안에서 사용할 추가적인 변수를 만드는 곳입니다.
ext {
    // 'springCloudVersion'이라는 이름으로 '2025.0.0'이라는 값을 정해두고, 나중에 필요할 때 이 이름을 가져다 쓰겠다는 의미입니다.
    set('springCloudVersion', "2025.0.0")
}

// dependencies: 이 프로젝트가 작동하는 데 필요한 구체적인 '부품(라이브러리) 목록'입니다.
// 마치 요리를 할 때 필요한 재료 목록과 같습니다.
dependencies {
    // 'implementation'은 프로그램이 만들어지고 실행될 때 모두 필요한 핵심 부품들을 의미합니다.
    // spring-boot-starter-*: 스프링 부트가 미리 여러 기능들을 묶어서 제공하는 꾸러미입니다.
    implementation 'org.springframework.boot:spring-boot-starter-actuator' // 애플리케이션의 상태를 확인하는 기능
    implementation 'org.springframework.boot:spring-boot-starter-data-jdbc' // 데이터베이스에 더 쉽게 접근하게 해주는 기능 (기본)
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa' // 데이터베이스 작업을 더 편리하게 해주는 고급 기능
    implementation 'org.springframework.boot:spring-boot-starter-jdbc' // 데이터베이스 연결을 위한 기본 기능
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client' // 카카오, 구글 등 다른 서비스의 로그인을 연동하는 기능
    implementation 'org.springframework.boot:spring-boot-starter-security' // 웹사이트의 보안을 담당하는 기능
    implementation 'org.springframework.boot:spring-boot-starter-web' // 웹사이트를 만드는 데 필요한 핵심 기능
    implementation 'org.flywaydb:flyway-core' // 데이터베이스의 변경 내역을 관리해주는 도구
    implementation 'org.flywaydb:flyway-mysql' // Flyway가 MySQL/MariaDB 데이터베이스와 작동하게 해주는 부품
    implementation 'org.liquibase:liquibase-core' // 데이터베이스 변경 내역 관리 도구 (Flyway와 유사)
    implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j' // 다른 서비스에 문제가 생겨도 내 서비스가 멈추지 않게 도와주는 기능

    // 'compileOnly'는 코드를 작성하고 컴파일할 때만 필요한 부품입니다. 프로그램 실행 시에는 포함되지 않아 앱 용량을 줄여줍니다.
    compileOnly 'org.projectlombok:lombok' // 반복적인 자바 코드를 줄여주는 편리한 도구

    // 'developmentOnly'는 개발할 때만 사용하고, 실제 서비스로 배포할 때는 제외되는 부품입니다.
    developmentOnly 'org.springframework.boot:spring-boot-devtools' // 코드 변경 시 자동으로 프로그램을 재시작해주는 개발 편의 기능
    developmentOnly 'org.springframework.boot:spring-boot-docker-compose' // 도커(Docker)라는 가상화 기술을 쉽게 사용하게 해주는 기능

    // 'runtimeOnly'는 프로그램이 실제로 실행될 때만 필요한 부품입니다.
    runtimeOnly 'io.micrometer:micrometer-registry-prometheus' // 프로그램의 상태 데이터를 수집하는 도구
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client' // MariaDB 데이터베이스에 연결하기 위한 프로그램

    // 'annotationProcessor'는 코드에 붙이는 특별한 표시(@)를 분석해서 추가 코드를 자동으로 만들어주는 역할을 합니다.
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor' // 설정 파일의 내용을 더 쉽게 사용하도록 도와줌
    annotationProcessor 'org.projectlombok:lombok' // 'compileOnly'의 lombok과 짝을 이뤄 작동합니다.

    // 'testImplementation'은 프로그램을 테스트하는 코드를 작성하고 실행할 때만 필요한 부품입니다.
    testImplementation 'org.springframework.boot:spring-boot-starter-test' // 스프링 부트 애플리케이션을 테스트하는 데 필요한 모든 기능 모음
    testImplementation 'org.springframework.security:spring-security-test' // 보안 관련 기능을 테스트하는 데 도움을 주는 기능
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher' // 테스트를 실행시켜주는 역할
}

// dependencyManagement: 여러 부품(라이브러리)들의 버전을 관리하는 곳입니다.
// 부품들끼리 버전이 맞지 않으면 문제가 생길 수 있는데, 이것을 방지해줍니다.
dependencyManagement {
    imports {
        // 'spring-cloud-dependencies'라는 부품 목록을 가져와서 버전 관리를 맡깁니다.
        // 이때 버전은 위에서 정한 'springCloudVersion' 변수를 사용합니다.
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}

// tasks: '빌드' 과정에서 실행될 여러 작업들에 대한 설정을 하는 곳입니다.
// 'test'라는 이름의 작업을 설정합니다.
tasks.named('test') {
    // 이 프로젝트의 테스트는 'JUnit Platform'이라는 최신 테스트 도구를 사용해서 실행하라고 알려줍니다.
    useJUnitPlatform()
}
