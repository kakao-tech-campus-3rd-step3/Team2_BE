# `@WithMockMember` 사용법 가이드

`@WithMockMember`는 Spring Security가 적용된 환경의 `@MvcSliceTest`에서, 복잡한 인증 절차 없이 **"로그인 된 사용자"**를 간단하게 시뮬레이션하기 위한 커스텀 어노테이션입니다.

## 🤔 왜 필요할까요?

컨트롤러 테스트 시, 특정 엔드포인트는 인증된 사용자가 요청한 상황을 가정해야 합니다. 하지만 이를 위해 매번 JWT 토큰을 만들고, `Authorization` 헤더에 담아 보내는 것은 매우 번거롭고 테스트의 본질을 흐립니다.

`@WithMockMember`는 이러한 번거로운 과정을 생략하고, 테스트 메서드에 어노테이션 하나만 붙이면 즉시 "로그인 된 상태"를 만들어주는 편리한 도구입니다.

## ⚙️ 어떻게 동작하나요? (우리말 풀이)

`@WithMockMember`는 테스트를 위한 **"VIP 패스"** 와 같습니다.

이 어노테이션을 테스트에 붙이면, 실제 보안 필터(`JwtAuthenticationFilter` 등)가 동작하기 전에 Spring Security의 `SecurityContext`에 가짜 사용자 인증 정보를 미리 등록해버립니다.

마치 파티장에 입장하기 전에 입구에서 신분증 검사를 받는 대신, "이 사람은 VIP 손님입니다"라는 명찰을 미리 달아주어 모든 보안 검사를 통과시키는 것과 같습니다.

## 📝 사용 방법

### 1. 기본 사용법

가장 간단한 방법은 테스트 메서드 위에 `@WithMockMember`를 붙이는 것입니다. 이렇게 하면 기본값으로 설정된 사용자로 로그인한 상태가 됩니다. (기본값: `memberId=1L`, `email="test@pullit.kr"`, `role=USER`)

```java
@Test
@WithMockMember
@DisplayName("로그인한 사용자는 자신의 정보를 성공적으로 조회한다")
void shouldRetrieveMyInfo() throws Exception {
    // given: memberId가 1L인 사용자가 이미 로그인했다고 가정
    given(memberPublicApi.getMemberInfo(1L)).willReturn(Optional.of(...));

    // when & then
    mockMvc.perform(get("/api/members/me"))
           .andExpect(status().isOk());
}
```

### 2. 사용자 정보 커스터마이징

테스트 시나리오에 따라 특정 사용자 정보를 설정해야 할 경우, 어노테이션의 속성을 이용해 값을 변경할 수 있습니다.

```java
@Test
@WithMockMember(memberId = 999L, email = "admin@pullit.kr", role = Role.ADMIN)
@DisplayName("관리자 권한으로 사용자 정보를 조회한다")
void shouldRetrieveInfoWithAdminRole() throws Exception {
    // 이제 컨트롤러의 @AuthenticationPrincipal Long memberId는 999L을 받게 됩니다.
    // ...
}
```

## ⚠️ 중요: 필터 제외 설정

`@WithMockMember`를 사용하여 이미 인증 상태를 만들었기 때문에, 실제 인증을 처리하는 `JwtAuthenticationFilter`는 테스트에 필요 없을 뿐만 아니라, 오히려 예기치 않은 문제를 일으킬 수 있습니다.

따라서 `@WithMockMember`를 사용하는 컨트롤러 테스트 클래스에는 반드시 `excludeFilters` 옵션을 사용하여 `JwtAuthenticationFilter`를 제외해야 합니다.

```java
@MvcSliceTest(
    controllers = MemberController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
class MemberControllerTest {
    // ... 테스트 코드 ...
}
```

이 설정을 통해 "오늘은 VIP 패스(`@WithMockMember`)를 사용하니, 입구의 보안요원(`JwtAuthenticationFilter`)은 잠시 쉬세요" 라고 명확하게 지정하여 테스트 환경의 충돌을 방지할 수 있습니다.
