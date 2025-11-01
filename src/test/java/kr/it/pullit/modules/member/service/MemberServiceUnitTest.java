package kr.it.pullit.modules.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.member.service.dto.SocialLoginCommand;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.builder.TestMemberBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@MockitoUnitTest
@DisplayName("MemberService 단위 테스트")
class MemberServiceUnitTest {

  @InjectMocks private MemberService memberService;

  @Mock private MemberRepository memberRepository;

  @Mock private CommonFolderPublicApi commonFolderPublicApi;

  @Test
  @DisplayName("새로운 사용자가 소셜 로그인을 하면, 새로운 Member를 생성하고 초기 폴더를 생성한다")
  void findOrCreateMember_createNewMember() {
    // given
    SocialLoginCommand command = SocialLoginCommand.kakao(12345L, "test@example.com", "Test User");
    when(memberRepository.findByKakaoId(command.kakaoId())).thenReturn(Optional.empty());
    when(memberRepository.findByEmail(command.email())).thenReturn(Optional.empty());
    when(memberRepository.save(any(Member.class)))
        .thenAnswer(
            invocation -> {
              Member memberToSave = invocation.getArgument(0);
              Member savedMember =
                  TestMemberBuilder.builder()
                      .kakaoId(memberToSave.getKakaoId())
                      .email(memberToSave.getEmail())
                      .name(memberToSave.getName())
                      .build();

              Field idField = Member.class.getDeclaredField("id");
              idField.setAccessible(true);
              idField.set(savedMember, 1L);

              return savedMember;
            });

    // when
    Optional<Member> result = memberService.findOrCreateMember(command);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getKakaoId()).isEqualTo(command.kakaoId());
    verify(commonFolderPublicApi, times(1)).createInitialFolders(any(Long.class));
  }
}
