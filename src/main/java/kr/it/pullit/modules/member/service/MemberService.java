package kr.it.pullit.modules.member.service;

import java.util.Optional;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.member.service.dto.SocialLoginCommand;
import kr.it.pullit.modules.member.web.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberPublicApi {

  private final MemberRepository memberRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<Member> findById(Long id) {
    return memberRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Member> findByKakaoId(Long kakaoId) {
    return memberRepository.findByKakaoId(kakaoId);
  }

  /** 카카오 앱이 변경되더라도 기존 멤버 정보를 그대로 사용할 수 있기 위해 조회 후 생성을 진행한다. */
  @Override
  @Transactional
  public Optional<Member> findOrCreateMember(SocialLoginCommand command) {
    Optional<Member> memberByKakao = memberRepository.findByKakaoId(command.kakaoId());
    if (memberByKakao.isPresent()) {
      return memberByKakao;
    }

    Member memberToReturn =
        memberRepository
            .findByEmail(command.email())
            .map(
                existing -> {
                  existing.linkKakaoId(command.kakaoId());
                  return memberRepository.save(existing);
                })
            .orElseGet(
                () -> {
                  Member newMember =
                      Member.create(command.kakaoId(), command.email(), command.name());
                  return memberRepository.save(newMember);
                });

    return Optional.of(memberToReturn);
  }

  @Override
  public Optional<Member> findByRefreshToken(String refreshToken) {
    return memberRepository.findByRefreshToken(refreshToken);
  }

  @Override
  public Member save(Member member) {
    return memberRepository.save(member);
  }

  @Override
  public Optional<MemberInfoResponse> getMemberInfo(Long memberId) {
    return memberRepository.findById(memberId).map(MemberInfoResponse::from);
  }
}
