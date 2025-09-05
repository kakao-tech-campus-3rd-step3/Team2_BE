package kr.it.pullit.modules.member.service;

import java.util.Optional;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberPublicApi {

  private final MemberRepository memberRepository;

  @Override
  public Optional<Member> findById(Long id) {
    return memberRepository.findById(id);
  }
}
