package kr.it.pullit.modules.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.support.annotation.JpaSliceTest;
import kr.it.pullit.support.builder.TestMemberBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@JpaSliceTest
@Import(MemberRepositoryImpl.class)
@DisplayName("MemberRepository 슬라이스 테스트")
class MemberRepositoryTest {

  @Autowired private MemberRepository memberRepository;

  private Member savedMember;

  @BeforeEach
  void setUp() {
    savedMember =
        memberRepository.save(
            TestMemberBuilder.builder()
                .email("test@example.com")
                .name("tester")
                .kakaoId(12345L)
                .build());
    savedMember.updateRefreshToken("test-refresh-token");
    memberRepository.save(savedMember);
  }

  @Nested
  @DisplayName("조회 테스트")
  class DescribeFind {

    @Test
    @DisplayName("ID로 회원을 조회하면, 저장된 회원이 조회되어야 한다")
    void shouldFindMemberById() {
      // when
      Optional<Member> foundMember = memberRepository.findById(savedMember.getId());

      // then
      assertThat(foundMember).isPresent();
      assertThat(foundMember.get().getId()).isEqualTo(savedMember.getId());
    }

    @Test
    @DisplayName("이메일로 회원을 조회하면, 저장된 회원이 조회되어야 한다")
    void shouldFindMemberByEmail() {
      // when
      Optional<Member> foundMember = memberRepository.findByEmail("test@example.com");

      // then
      assertThat(foundMember).isPresent();
      assertThat(foundMember.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Kakao ID로 회원을 조회하면, 저장된 회원이 조회되어야 한다")
    void shouldFindMemberByKakaoId() {
      // when
      Optional<Member> foundMember = memberRepository.findByKakaoId(12345L);

      // then
      assertThat(foundMember).isPresent();
      assertThat(foundMember.get().getKakaoId()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("리프레시 토큰으로 회원을 조회하면, 저장된 회원이 조회되어야 한다")
    void shouldFindMemberByRefreshToken() {
      // when
      Optional<Member> foundMember = memberRepository.findByRefreshToken("test-refresh-token");

      // then
      assertThat(foundMember).isPresent();
      assertThat(foundMember.get().getRefreshToken()).isEqualTo("test-refresh-token");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면, Optional.empty()가 반환되어야 한다")
    void shouldReturnEmptyWhenMemberNotFound() {
      // when
      Optional<Member> foundMember = memberRepository.findByEmail("non-existent@example.com");

      // then
      assertThat(foundMember).isEmpty();
    }
  }
}
