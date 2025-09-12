package kr.it.pullit.modules.member.service;

import java.util.Optional;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.MemberStatus;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.member.web.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberPublicApi {

  private final MemberRepository memberRepository;

  @Override
  public Optional<Member> findById(Long id) {
    return memberRepository.findById(id);
  }

  @Override
  public Optional<Member> findByKakaoId(Long kakaoId) {
    return memberRepository.findByKakaoId(kakaoId);
  }

  @Override
  public Member create(Member member) {
    return memberRepository.save(member);
  }

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
}
