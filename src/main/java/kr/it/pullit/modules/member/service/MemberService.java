package kr.it.pullit.modules.member.service;

import java.util.Optional;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.MemberStatus;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.member.service.dto.SocialLoginCommand;
import kr.it.pullit.modules.member.web.dto.SignUpRequest;
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

  //TODO: REMOVE THIS METHOD.
  @Override
  public Member create(Member member) {
    return memberRepository.save(member);
  }

  //TODO: REMOVE THIS METHOD.
  @Transactional
  public Member signup(SignUpRequest request) {
    Member newMember =
        Member.builder()
            .kakaoId(request.kakaoId())
            .email(request.email())
            .name(request.name())
            .status(MemberStatus.ACTIVE)
            .build();
    return memberRepository.save(newMember);
  }

  @Override
  @Transactional
  public Member findOrCreateMember(SocialLoginCommand command) {
    return memberRepository
        .findByKakaoId(command.kakaoId())
        .orElseGet(
            () ->
                memberRepository.save(
                    Member.builder()
                        .kakaoId(command.kakaoId())
                        .email(command.email())
                        .name(command.name())
                        .status(MemberStatus.ACTIVE)
                        .build()));
  }

  @Override
  public Optional<Member> findByRefreshToken(String refreshToken) {
    return memberRepository.findByRefreshToken(refreshToken);
  }

  @Override
  public Member save(Member member) {
    return memberRepository.save(member);
  }
}
