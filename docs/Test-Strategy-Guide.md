# Pullit 프로젝트 4대 테스트 상세 가이드

이 문서는 Pullit 프로젝트에서 사용하는 4가지 테스트 유형(`단위`, `JPA 슬라이스`, `MVC 슬라이스`, `통합`)에 대한 상세 가이드입니다. 새로운 테스트를 작성하기 전에, 테스트하려는 대상의 책임과 목적에 가장 적합한 유형을 선택하여 적용해주시기 바랍니다.

## 🎯 테스트 핵심 철학

> **"테스트 대상의 핵심 책임에 집중하고, 외부 의존성은 격리한다."**

좋은 테스트는 검증하려는 범위를 명확히 한정할 때 만들어집니다. 우리는 테스트 대상을 명확히 고립시키기 위해 Mocking과 테스트 전용 설정을 적극적으로 활용합니다.

---

## 1. 단위 테스트 (Unit Test)

### 가. 목적 (When to use?)

**하나의 클래스(또는 메서드)가 가진 순수한 로직을 외부 환경의 영향 없이 독립적으로 검증**하고 싶을 때 사용합니다. 스프링 컨텍스트를 로드하지 않아 가장 빠르고 가볍습니다.

-   **주요 대상:** 도메인 객체(Entity, VO), 순수 로직을 가진 서비스/유틸리티 클래스 등

### 나. 핵심 어노테이션: `@UnitTest`

`@UnitTest`는 다음과 같은 설정을 포함하는 커스텀 어노테이션입니다.
-   `@ExtendWith(MockitoExtension.class)`: JUnit5에서 Mockito 프레임워크를 사용하게 해줍니다. (`@Mock`, `@InjectMocks`)
-   `@Import({FixedClockConfig.class, ...})`: 테스트에 필요한 최소한의 설정(고정된 시간 등)을 가져옵니다.

### 다. 작성 예시: `JwtAuthenticatorTest.java`

`JwtAuthenticator`는 토큰 문자열을 받아 그 유효성을 검증하고 인증 객체를 만드는 순수한 책임을 가집니다. 외부 의존성인 `JwtTokenPort`는 `@Mock`으로 가짜 객체를 만들어, `JwtAuthenticator`의 로직에만 집중하여 테스트합니다.

```java
package kr.it.pullit.platform.security.jwt;

// ... import 생략 ...
import kr.it.pullit.support.annotation.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@UnitTest
@DisplayName("JwtAuthenticator 단위 테스트")
class JwtAuthenticatorTest {

  @InjectMocks private JwtAuthenticator jwtAuthenticator; // 테스트 대상
  @Mock private JwtTokenPort jwtTokenPort; // 가짜로 대체할 의존성
  @Mock private DecodedJWT decodedJwt;

  @Test
  @DisplayName("유효한 토큰이 제공되면 Success 결과를 반환한다")
  void shouldReturnSuccessWhenTokenIsValid() {
    // given: jwtTokenPort가 특정 입력에 대해 미리 정해진 결과를 반환하도록 설정
    String token = "valid-token";
    when(jwtTokenPort.validateToken(token)).thenReturn(new TokenValidationResult.Valid(decodedJwt));
    // ... 추가적인 given 설정 ...

    // when: 테스트 대상 메서드 실행
    AuthenticationResult result = jwtAuthenticator.authenticate(token);

    // then: 실행 결과 검증
    assertThat(result).isInstanceOf(AuthenticationResult.Success.class);
  }
}
```

---

## 2. JPA 슬라이스 테스트 (JPA Slice Test)

### 가. 목적 (When to use?)

**JPA Repository와 Entity 간의 상호작용을 검증**하고 싶을 때 사용합니다. 실제 DB 대신 H2 같은 인메모리 DB를 사용하여 DB 계층만 고립시켜 테스트합니다.

-   **주요 대상:** JPA Repository 인터페이스, QueryDSL 구현체
-   **핵심 검증 포인트:** Entity 매핑은 올바른가? 작성한 쿼리(JPQL, QueryDSL)는 정상 동작하는가?

### 나. 핵심 어노테이션: `@JpaSliceTest`

`@JpaSliceTest`는 `@DataJpaTest`를 포함하며, JPA 관련 설정만 로드하여 테스트 환경을 구성합니다.

### 다. 작성 예시: `MemberRepositoryTest.java`

`Member` 엔티티를 `memberRepository`를 통해 저장하고, 우리가 정의한 `findByEmail` 쿼리 메서드가 올바르게 동작하는지 검증합니다.

```java
package kr.it.pullit.modules.member.repository;

// ... import 생략 ...
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.support.annotation.JpaSliceTest;
import kr.it.pullit.support.builder.TestMemberBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@JpaSliceTest
@DisplayName("MemberRepository 슬라이스 테스트")
class MemberRepositoryTest {

  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("회원을 저장하고 이메일로 조회하면, 저장된 회원이 조회되어야 한다")
  void shouldSaveAndFindMemberByEmail() {
    // given: 테스트용 Member 엔티티 생성 및 저장
    Member frodo = TestMemberBuilder.builder().withEmail("frodo@example.com").build();
    memberRepository.save(frodo);

    // when: 이메일로 회원 조회
    Optional<Member> foundFrodo = memberRepository.findByEmail("frodo@example.com");

    // then: 조회 결과 검증
    assertThat(foundFrodo).isPresent();
    assertThat(foundFrodo.get().getEmail()).isEqualTo("frodo@example.com");
  }
}
```

---

## 3. MVC 슬라이스 테스트 (MVC Slice Test)

### 가. 목적 (When to use?)

**Controller의 API 동작을 웹 계층에 한정하여 검증**하고 싶을 때 사용합니다. 실제 서버를 띄우지 않고 `MockMvc`를 통해 HTTP 요청/응답을 시뮬레이션합니다. 서비스 계층은 `@MockBean`으로 격리합니다.

-   **주요 대상:** Controller 클래스
-   **핵심 검증 포인트:** 요청 매핑, 데이터 바인딩, 입력값 검증, 성공/실패 응답 형식, 예외 처리 등

### 나. 핵심 어노테이션

-   **`@AuthenticatedMvcSliceTest` (권장):** 인증이 필요한 거의 모든 API를 테스트할 때 사용합니다.
-   **`@MvcSliceTest`:** 리프레시토큰검사와 같은 인증받기전 및 인증이 필요없는 API를 테스트할 때 사용합니다.

### 다. 작성 예시: `SourceControllerTest.java`

`SourceController`의 파일 업로드 URL 생성 API를 테스트합니다. 의존성인 `SourcePublicApi`는 `@MockBean`으로 만들고, `@WithMockMember`로 인증된 사용자를 시뮬레이션합니다.

`ControllerTest`를 상속받아 MockMvc, ObjectMapper, CookieManager를 상속받습니다.
쿠키매니저는 MvcTest가 테스트에 필요한 최소한의 Bean만 로드할 때 가짜 보안 관련 Bean을 만들 때 필요해서 주입해주는 것으로 신경안써도 됩니다.

```java
package kr.it.pullit.integration.modules.learningsource.source.web;

// ... import 생략 ...
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AuthenticatedMvcSliceTest(controllers = SourceController.class)
public class SourceControllerTest extends ControllerTest {

  @MockitoBean private SourcePublicApi sourcePublicApi;

  @Test
  @WithMockMember // memberId=1L인 사용자가 인증된 상황을 시뮬레이션
  @DisplayName("학습 자료 업로드 URL을 성공적으로 생성한다")
  void shouldGenerateUploadUrlSuccessfully() throws Exception {
    // given: sourcePublicApi가 특정 입력에 대해 정해진 응답을 반환하도록 설정
    var request = new SourceUploadRequest("test.pdf", "application/pdf", 1234L);
    var mockResponse = new SourceUploadResponse(...);
    given(sourcePublicApi.generateUploadUrl(..., 1L)).willReturn(mockResponse);

    // when & then: MockMvc로 API를 호출하고 응답을 검증
    mockMvc
        .perform(post("/api/learning/source/upload")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uploadUrl").value("https://s3.example.com/upload"));
  }
}
```

---

## 4. 통합 테스트 (Integration Test)

### 가. 목적 (When to use?)

**서비스(`Service`) 계층의 비즈니스 시나리오가 여러 컴포넌트와 상호작용하며 올바르게 동작하는지 검증**하고 싶을 때 사용합니다.

이론적으로 통합 테스트는 컨트롤러부터 데이터베이스까지 모든 계층을 아우를 수 있지만, 현실적으로 모든 경우의 수를 재현하기는 어렵습니다. 따라서 우리 프로젝트에서는 통합 테스트의 주목적을 **서비스의 핵심 책임(트랜잭션 관리, 여러 컴포넌트 조합)을 실제와 가장 유사한 환경에서 검증하는 것**으로 한정합니다.

-   **주요 대상:** Service 클래스

### 나. 핵심 어노테이션: `@IntegrationTest`

`@IntegrationTest`는 `@SpringBootTest`를 포함하며, 테스트 프로필에 설정된 실제 데이터베이스(예: H2)에 연결하는 등 완전한 애플리케이션 컨텍스트를 구성합니다. 이를 통해 서비스 메서드 호출 시 실제 트랜잭션과 DB 쿼리가 발생하는 상황을 시뮬레이션할 수 있습니다.

### 다. 작성 예시: (SourceServiceTest.java 가상 예시)

`SourceService`의 파일 업로드 완료 로직을 테스트합니다. 실제 DB에 `Source` 데이터가 저장되고 상태가 변경되는지를 검증합니다.

```java
// 이 코드는 이해를 돕기 위한 가상 예시입니다.
package kr.it.pullit.integration.modules.learningsource.source.service;

import kr.it.pullit.support.annotation.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class SourceServiceIntegrationTest {

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    @DisplayName("파일 업로드 완료 처리가 성공하면 Source의 상태가 READY로 변경된다")
    void shouldChangeSourceStatusToReadyOnUploadComplete() {
        // given: 테스트에 필요한 회원, 폴더 등의 데이터를 미리 DB에 저장
        Long memberId = 1L;
        // ...

        // when: 테스트 대상 서비스 메서드 호출
        var request = new SourceUploadCompleteRequest(...);
        sourceService.processUploadComplete(request, memberId);

        // then: DB에서 직접 데이터를 조회하여 상태가 올바르게 변경되었는지 검증
        Source foundSource = sourceRepository.findByFilePath(request.getFilePath()).get();
        assertThat(foundSource.getStatus()).isEqualTo(SourceStatus.READY);
    }
}
```
